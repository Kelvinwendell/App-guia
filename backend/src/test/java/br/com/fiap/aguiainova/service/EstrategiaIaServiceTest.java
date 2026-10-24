package br.com.fiap.aguiainova.service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import br.com.fiap.aguiainova.config.AppProperties;
import br.com.fiap.aguiainova.dto.request.AnaliseEstrategiaRequest;
import br.com.fiap.aguiainova.dto.request.EstrategiaRequest;
import br.com.fiap.aguiainova.dto.request.RascunhoEstrategiaRequest;
import br.com.fiap.aguiainova.exception.LimiteDeRequisicoesExcedidoException;
import br.com.fiap.aguiainova.exception.ServicoIndisponivelException;
import br.com.fiap.aguiainova.integration.ai.LlmClient;
import br.com.fiap.aguiainova.integration.ai.LlmNaoConfiguradoException;
import br.com.fiap.aguiainova.mapper.EstrategiaMapper;
import br.com.fiap.aguiainova.model.enums.Perfil;
import br.com.fiap.aguiainova.repository.EstrategiaRepository;
import br.com.fiap.aguiainova.security.LimitadorDeTaxa;
import br.com.fiap.aguiainova.security.UsuarioLogado;
import br.com.fiap.aguiainova.service.ia.AnaliseGerada;
import br.com.fiap.aguiainova.service.ia.AnalisadorDeEstrategias;
import br.com.fiap.aguiainova.service.ia.InterpretadorRespostaIa;
import br.com.fiap.aguiainova.service.ia.LeitorDeSaude;
import br.com.fiap.aguiainova.service.ia.LeituraGerada;
import br.com.fiap.aguiainova.service.ia.PromptsIa;
import br.com.fiap.aguiainova.support.Dados;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** O limite de chamadas de IA por minuto vale para o rascunho, a critica e a leitura de saude de estrategias. */
class EstrategiaIaServiceTest {

    private static final int LIMITE_POR_MINUTO = 3;
    private static final UsuarioLogado LIDERANCA = new UsuarioLogado("u-1", "Gabriela", "g@teste.com", Perfil.LIDERANCA);
    private static final UsuarioLogado OUTRA_LIDERANCA = new UsuarioLogado("u-2", "Outra", "o@teste.com", Perfil.LIDERANCA);

    private final LlmClient llm = mock(LlmClient.class);
    private final AnalisadorDeEstrategias analisador = mock(AnalisadorDeEstrategias.class);
    private final LeitorDeSaude leitor = mock(LeitorDeSaude.class);
    private final EstrategiaSaudeService saude = mock(EstrategiaSaudeService.class);
    private final EstrategiaMapper mapper = mock(EstrategiaMapper.class);
    private EstrategiaIaService servico;

    @BeforeEach
    void preparar() {
        Clock relogio = Clock.fixed(Instant.parse("2026-10-24T12:00:00Z"), ZoneOffset.UTC);
        AppProperties propriedades = comLimite(LIMITE_POR_MINUTO);
        servico = new EstrategiaIaService(llm, new PromptsIa(), new InterpretadorRespostaIa(JsonMapper.builder().build()),
                analisador, leitor, mock(EstrategiaRepository.class), saude, mapper, relogio,
                new LimitadorDeTaxa(relogio), propriedades);
        when(llm.gerar(any())).thenThrow(new LlmNaoConfiguradoException());
        when(analisador.analisar(any(), anyList())).thenReturn(
                new AnaliseGerada(70, "ok", List.of(), List.of(), List.of(), "HEURISTICA"));
        when(leitor.ler(any())).thenReturn(new LeituraGerada("ok", List.of(), "HEURISTICA"));
    }

    private static AppProperties comLimite(int iaPorMinuto) {
        AppProperties base = Dados.propriedades(false, false, "", "gemini");
        return new AppProperties(base.jwt(), base.security(), base.seed(), base.ai(),
                new AppProperties.RateLimit(1000, iaPorMinuto), base.dashboard());
    }

    private void rascunhar(UsuarioLogado usuario) {
        servico.rascunhar(new RascunhoEstrategiaRequest("Reduzir o desperdício"), usuario);
    }

    private void analisar(UsuarioLogado usuario) {
        servico.analisar(new AnaliseEstrategiaRequest(null,
                new EstrategiaRequest("Título da estratégia", "Descrição", "Geral", null, null)), usuario);
    }

    private void lerSaude(UsuarioLogado usuario) {
        servico.lerSaude("e-1", usuario);
    }

    @Test
    void rascunhoAcimaDoLimiteRecebe429SemChamarOModelo() {
        for (int i = 0; i < LIMITE_POR_MINUTO; i++) {
            assertThatThrownBy(() -> rascunhar(LIDERANCA)).isInstanceOf(ServicoIndisponivelException.class);
        }

        assertThatThrownBy(() -> rascunhar(LIDERANCA)).isInstanceOf(LimiteDeRequisicoesExcedidoException.class);
        verify(llm, times(LIMITE_POR_MINUTO)).gerar(any());
    }

    @Test
    void criticaAcimaDoLimiteRecebe429SemChamarAnalisador() {
        for (int i = 0; i < LIMITE_POR_MINUTO; i++) {
            analisar(LIDERANCA);
        }

        assertThatThrownBy(() -> analisar(LIDERANCA)).isInstanceOf(LimiteDeRequisicoesExcedidoException.class);
        verify(analisador, times(LIMITE_POR_MINUTO)).analisar(any(), anyList());
    }

    @Test
    void leituraDaSaudeAcimaDoLimiteRecebe429SemConsultarNada() {
        for (int i = 0; i < LIMITE_POR_MINUTO; i++) {
            lerSaude(LIDERANCA);
        }

        assertThatThrownBy(() -> lerSaude(LIDERANCA)).isInstanceOf(LimiteDeRequisicoesExcedidoException.class);
        verify(saude, times(LIMITE_POR_MINUTO)).diagnosticar("e-1");
        verify(leitor, times(LIMITE_POR_MINUTO)).ler(any());
    }

    @Test
    void asTresOperacoesDividemOMesmoLimiteDoUsuario() {
        assertThatThrownBy(() -> rascunhar(LIDERANCA)).isInstanceOf(ServicoIndisponivelException.class);
        analisar(LIDERANCA);
        lerSaude(LIDERANCA);

        assertThatThrownBy(() -> rascunhar(LIDERANCA)).isInstanceOf(LimiteDeRequisicoesExcedidoException.class);
        assertThatThrownBy(() -> analisar(LIDERANCA)).isInstanceOf(LimiteDeRequisicoesExcedidoException.class);
        assertThatThrownBy(() -> lerSaude(LIDERANCA)).isInstanceOf(LimiteDeRequisicoesExcedidoException.class);
    }

    @Test
    void oLimiteEPorUsuario() {
        for (int i = 0; i < LIMITE_POR_MINUTO; i++) {
            analisar(LIDERANCA);
        }
        assertThatThrownBy(() -> analisar(LIDERANCA)).isInstanceOf(LimiteDeRequisicoesExcedidoException.class);

        analisar(OUTRA_LIDERANCA);
    }

    @Test
    void chamadaRecusadaPeloLimiteNaoTocaNoRestoDoSistema() {
        AppProperties semFolga = comLimite(1);
        Clock relogio = Clock.fixed(Instant.parse("2026-10-24T12:00:00Z"), ZoneOffset.UTC);
        EstrategiaIaService estrito = new EstrategiaIaService(llm, new PromptsIa(),
                new InterpretadorRespostaIa(JsonMapper.builder().build()), analisador, leitor,
                mock(EstrategiaRepository.class), saude, mapper, relogio, new LimitadorDeTaxa(relogio), semFolga);
        estrito.lerSaude("e-1", LIDERANCA);

        assertThatThrownBy(() -> estrito.lerSaude("e-2", LIDERANCA))
                .isInstanceOf(LimiteDeRequisicoesExcedidoException.class)
                .hasMessageContaining("Muitas chamadas de IA");
        verify(saude, times(1)).diagnosticar(any());
        verifyNoInteractions(llm);
    }
}
