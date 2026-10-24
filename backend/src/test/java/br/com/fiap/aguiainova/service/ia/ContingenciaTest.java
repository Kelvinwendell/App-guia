package br.com.fiap.aguiainova.service.ia;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import br.com.fiap.aguiainova.dto.response.ResumoDashboardResponse;
import br.com.fiap.aguiainova.dto.response.ResumoIdeiasResponse;
import br.com.fiap.aguiainova.integration.ai.LlmIndisponivelException;
import br.com.fiap.aguiainova.integration.ai.LlmNaoConfiguradoException;
import br.com.fiap.aguiainova.model.AvaliacaoIa;
import br.com.fiap.aguiainova.model.DadosEstrategia;
import br.com.fiap.aguiainova.model.DiagnosticoEstrategia;
import br.com.fiap.aguiainova.model.Estrategia;
import br.com.fiap.aguiainova.model.Ideia;
import br.com.fiap.aguiainova.model.SaudeEstrategia;
import br.com.fiap.aguiainova.model.enums.Prioridade;
import br.com.fiap.aguiainova.support.Dados;
import br.com.fiap.aguiainova.support.IndicadoresBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Os decorators de contingencia: a IA generativa primeiro, as regras locais quando ela nao entrega. */
@ExtendWith(MockitoExtension.class)
class ContingenciaTest {

    private static final Instant AGORA = Instant.parse("2026-06-15T12:00:00Z");

    @Mock
    private AvaliadorLlm avaliadorLlm;
    @Mock
    private AvaliadorHeuristico avaliadorHeuristico;
    @Mock
    private GeradorInsightsLlm geradorLlm;
    @Mock
    private GeradorInsightsHeuristico geradorHeuristico;
    @Mock
    private AnalisadorEstrategiasLlm analisadorLlm;
    @Mock
    private AnalisadorEstrategiasHeuristico analisadorHeuristico;
    @Mock
    private LeitorSaudeLlm leitorLlm;
    @Mock
    private LeitorSaudeHeuristico leitorHeuristico;

    private final Ideia a = Dados.ideia("a", "A", "Descrição A", "Geral", null);
    private final Ideia b = Dados.ideia("b", "B", "Descrição B", "Geral", null);
    private final List<Estrategia> vigentes = List.of();

    private static AvaliacaoIa nota(String fonte) {
        return new AvaliacaoIa(70, 60, Prioridade.ALTA, "ok", List.of(), List.of(), fonte, AGORA);
    }

    @Test
    void seOModeloAvaliaTudoAsRegrasNemSaoConsultadas() {
        when(avaliadorLlm.avaliar(List.of(a, b), vigentes)).thenReturn(Map.of("a", nota("GEMINI"), "b", nota("GEMINI")));

        Map<String, AvaliacaoIa> resultado = new AvaliadorComContingencia(avaliadorLlm, avaliadorHeuristico)
                .avaliar(List.of(a, b), vigentes);

        assertThat(resultado.values()).extracting(AvaliacaoIa::fonte).containsOnly("GEMINI");
        verifyNoInteractions(avaliadorHeuristico);
    }

    @Test
    void asRegrasCobremSomenteAsIdeiasQueOModeloNaoDevolveu() {
        when(avaliadorLlm.avaliar(List.of(a, b), vigentes)).thenReturn(Map.of("a", nota("GEMINI")));
        when(avaliadorHeuristico.avaliar(List.of(b), vigentes)).thenReturn(Map.of("b", nota("HEURISTICA")));

        Map<String, AvaliacaoIa> resultado = new AvaliadorComContingencia(avaliadorLlm, avaliadorHeuristico)
                .avaliar(List.of(a, b), vigentes);

        assertThat(resultado.get("a").fonte()).isEqualTo("GEMINI");
        assertThat(resultado.get("b").fonte()).isEqualTo("HEURISTICA");
    }

    @Test
    void seOModeloFalhaOuNaoEstaConfiguradoAsRegrasAvaliamTudo() {
        when(avaliadorHeuristico.avaliar(List.of(a, b), vigentes))
                .thenReturn(Map.of("a", nota("HEURISTICA"), "b", nota("HEURISTICA")));
        var contingencia = new AvaliadorComContingencia(avaliadorLlm, avaliadorHeuristico);

        doThrow(new LlmIndisponivelException("HTTP 429")).when(avaliadorLlm).avaliar(anyList(), anyList());
        assertThat(contingencia.avaliar(List.of(a, b), vigentes)).hasSize(2);

        doThrow(new LlmNaoConfiguradoException()).when(avaliadorLlm).avaliar(anyList(), anyList());
        assertThat(contingencia.avaliar(List.of(a, b), vigentes)).hasSize(2);
    }

    @Test
    void erroInesperadoNaoEEngolido() {
        when(avaliadorLlm.avaliar(anyList(), anyList())).thenThrow(new IllegalStateException("bug"));

        assertThatThrownBy(() -> new AvaliadorComContingencia(avaliadorLlm, avaliadorHeuristico)
                .avaliar(List.of(a), vigentes)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void insightsUsamOModeloEDepoisAsRegras() {
        var indicadores = new IndicadoresDashboard(resumoVazio(), List.of(), List.of());
        var doModelo = new InsightsGerados("modelo", List.of("i"), List.of("r"), "GEMINI");
        var dasRegras = new InsightsGerados("regras", List.of("i"), List.of("r"), FonteIa.HEURISTICA);
        when(geradorHeuristico.gerar(indicadores)).thenReturn(dasRegras);
        var contingencia = new GeradorInsightsComContingencia(geradorLlm, geradorHeuristico);

        when(geradorLlm.gerar(indicadores)).thenReturn(doModelo);
        assertThat(contingencia.gerar(indicadores)).isSameAs(doModelo);
        verify(geradorHeuristico, never()).gerar(any());

        doThrow(new LlmIndisponivelException("timeout")).when(geradorLlm).gerar(indicadores);
        assertThat(contingencia.gerar(indicadores)).isSameAs(dasRegras);

        doThrow(new LlmNaoConfiguradoException()).when(geradorLlm).gerar(indicadores);
        assertThat(contingencia.gerar(indicadores)).isSameAs(dasRegras);
        verify(geradorHeuristico, times(2)).gerar(eq(indicadores));
    }

    @Test
    void analiseDeEstrategiaUsaOModeloEDepoisAsRegras() {
        var dados = new DadosEstrategia("Título da estratégia", "Descrição", "Geral", null, null);
        var doModelo = new AnaliseGerada(80, "modelo", List.of(), List.of(), List.of(), "GEMINI");
        var dasRegras = new AnaliseGerada(60, "regras", List.of(), List.of(), List.of(), FonteIa.HEURISTICA);
        when(analisadorHeuristico.analisar(dados, vigentes)).thenReturn(dasRegras);
        var contingencia = new AnalisadorEstrategiasComContingencia(analisadorLlm, analisadorHeuristico);

        when(analisadorLlm.analisar(dados, vigentes)).thenReturn(doModelo);
        assertThat(contingencia.analisar(dados, vigentes)).isSameAs(doModelo);
        verify(analisadorHeuristico, never()).analisar(any(), anyList());

        doThrow(new LlmIndisponivelException("timeout")).when(analisadorLlm).analisar(dados, vigentes);
        assertThat(contingencia.analisar(dados, vigentes)).isSameAs(dasRegras);

        doThrow(new LlmNaoConfiguradoException()).when(analisadorLlm).analisar(dados, vigentes);
        assertThat(contingencia.analisar(dados, vigentes)).isSameAs(dasRegras);
    }

    @Test
    void leituraDaSaudeUsaOModeloEDepoisAsRegras() {
        var indicadores = IndicadoresBuilder.saudavel().build();
        var diagnostico = new DiagnosticoEstrategia(Dados.estrategia("e-1", "T", "D", "C", null), indicadores,
                SaudeEstrategia.avaliar(indicadores, IndicadoresBuilder.HOJE));
        var doModelo = new LeituraGerada("modelo", List.of("a"), "GEMINI");
        var dasRegras = new LeituraGerada("regras", List.of("b"), FonteIa.HEURISTICA);
        when(leitorHeuristico.ler(diagnostico)).thenReturn(dasRegras);
        var contingencia = new LeitorSaudeComContingencia(leitorLlm, leitorHeuristico);

        when(leitorLlm.ler(diagnostico)).thenReturn(doModelo);
        assertThat(contingencia.ler(diagnostico)).isSameAs(doModelo);
        verify(leitorHeuristico, never()).ler(any());

        doThrow(new LlmIndisponivelException("HTTP 429")).when(leitorLlm).ler(diagnostico);
        assertThat(contingencia.ler(diagnostico)).isSameAs(dasRegras);

        doThrow(new LlmNaoConfiguradoException()).when(leitorLlm).ler(diagnostico);
        assertThat(contingencia.ler(diagnostico)).isSameAs(dasRegras);
    }

    private static ResumoDashboardResponse resumoVazio() {
        return new ResumoDashboardResponse(null, null, null, null, null, null, 0, 0, Map.of(), Map.of(),
                new ResumoIdeiasResponse(0, 0, 0, 0, null), List.of());
    }
}
