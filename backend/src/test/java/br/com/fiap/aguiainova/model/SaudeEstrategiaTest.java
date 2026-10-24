package br.com.fiap.aguiainova.model;

import java.math.BigDecimal;

import br.com.fiap.aguiainova.model.enums.NivelSaude;
import br.com.fiap.aguiainova.support.IndicadoresBuilder;
import org.junit.jupiter.api.Test;

import static br.com.fiap.aguiainova.support.IndicadoresBuilder.HOJE;
import static org.assertj.core.api.Assertions.assertThat;

/** As regras que decidem se uma estrategia esta em dia, em risco ou sem tracao (e o que fazer a respeito). */
class SaudeEstrategiaTest {

    private static SaudeEstrategia avaliar(IndicadoresBuilder indicadores) {
        return SaudeEstrategia.avaliar(indicadores.build(), HOJE);
    }

    @Test
    void comMovimentoEmAbertoESemProblemasEstaEmDia() {
        SaudeEstrategia saude = avaliar(IndicadoresBuilder.saudavel());

        assertThat(saude.nivel()).isEqualTo(NivelSaude.EM_DIA);
        assertThat(saude.motivos()).containsExactly("3 ideia(s) e 2 projeto(s), 1 em aberto, sem atrasos.");
        assertThat(saude.proximaAcao()).isEqualTo("Mantenha o acompanhamento e registre os resultados dos projetos.");
    }

    @Test
    void projetoAtrasadoColocaEmRiscoEPedeRevisaoDoPrazo() {
        SaudeEstrategia saude = avaliar(IndicadoresBuilder.saudavel().projetos(2, 2, 1, 0));

        assertThat(saude.nivel()).isEqualTo(NivelSaude.EM_RISCO);
        assertThat(saude.motivos()).containsExactly("1 projeto(s) com o prazo vencido.");
        assertThat(saude.proximaAcao()).startsWith("Reveja o prazo");
    }

    @Test
    void periodoVencidoComEstrategiaAindaVigenteColocaEmRisco() {
        SaudeEstrategia saude = avaliar(IndicadoresBuilder.saudavel().periodo(HOJE.minusDays(200), HOJE.minusDays(1)));

        assertThat(saude.nivel()).isEqualTo(NivelSaude.EM_RISCO);
        assertThat(saude.motivos()).containsExactly("O período terminou em 23/10/2026 e a estratégia continua vigente.");
        assertThat(saude.proximaAcao()).isEqualTo("Encerre a estratégia ou prorrogue o período.");
    }

    @Test
    void periodoQueTerminaHojeAindaNaoEstaVencido() {
        assertThat(avaliar(IndicadoresBuilder.saudavel().periodo(HOJE.minusDays(200), HOJE)).nivel())
                .isEqualTo(NivelSaude.EM_DIA);
    }

    @Test
    void umaIdeiaAprovadaSemProjetoSoAvisaMasDuasColocamEmRisco() {
        SaudeEstrategia uma = avaliar(IndicadoresBuilder.saudavel().aprovadasSemProjeto(1));
        SaudeEstrategia duas = avaliar(IndicadoresBuilder.saudavel().aprovadasSemProjeto(2));

        assertThat(uma.nivel()).isEqualTo(NivelSaude.EM_DIA);
        assertThat(uma.motivos()).contains("1 ideia(s) aprovada(s) ainda sem projeto.");
        assertThat(uma.proximaAcao()).isEqualTo("Transforme as ideias aprovadas em projetos.");
        assertThat(duas.nivel()).isEqualTo(NivelSaude.EM_RISCO);
    }

    @Test
    void roiNegativoSoPreocupaQuandoJaHaProjetoConcluido() {
        BigDecimal negativo = new BigDecimal("-40.00");
        SaudeEstrategia comConcluido = avaliar(IndicadoresBuilder.saudavel().projetos(2, 1, 0, 1)
                .financeiro(new BigDecimal("1000"), new BigDecimal("600"), negativo));
        SaudeEstrategia semConcluido = avaliar(IndicadoresBuilder.saudavel().projetos(2, 2, 0, 0)
                .financeiro(new BigDecimal("1000"), new BigDecimal("600"), negativo));

        assertThat(comConcluido.nivel()).isEqualTo(NivelSaude.EM_RISCO);
        assertThat(comConcluido.motivos()).contains("ROI de -40% mesmo com projeto(s) já concluído(s).");
        assertThat(comConcluido.proximaAcao()).startsWith("Reveja escopo");
        assertThat(semConcluido.nivel()).isEqualTo(NivelSaude.EM_DIA);
    }

    @Test
    void semIdeiasNemProjetosDepoisDaCarenciaFicaSemTracao() {
        SaudeEstrategia saude = avaliar(IndicadoresBuilder.semVinculos().criadaEm(HOJE.minusDays(15)));

        assertThat(saude.nivel()).isEqualTo(NivelSaude.SEM_TRACAO);
        assertThat(saude.motivos()).contains("Nenhuma ideia ou projeto vinculado desde que foi criada.");
        assertThat(saude.proximaAcao()).startsWith("Divulgue a estratégia");
    }

    @Test
    void estrategiaRecemCriadaGanhaCarencia() {
        SaudeEstrategia saude = avaliar(IndicadoresBuilder.semVinculos().criadaEm(HOJE.minusDays(14)));

        assertThat(saude.nivel()).isEqualTo(NivelSaude.EM_DIA);
        assertThat(saude.motivos()).contains("Estratégia recente: ainda sem ideias ou projetos vinculados.");
    }

    @Test
    void semProjetoEmAbertoEComMaisDeSessentaDiasParadaFicaSemTracao() {
        IndicadoresBuilder parada = IndicadoresBuilder.saudavel().projetos(1, 0, 0, 1);

        SaudeEstrategia emSessenta = avaliar(parada.ultimaAtividade(HOJE.minusDays(60)));
        SaudeEstrategia emSessentaEUm = avaliar(parada.ultimaAtividade(HOJE.minusDays(61)));

        assertThat(emSessenta.nivel()).isEqualTo(NivelSaude.EM_DIA);
        assertThat(emSessentaEUm.nivel()).isEqualTo(NivelSaude.SEM_TRACAO);
        assertThat(emSessentaEUm.motivos()).contains("Sem atividade há 61 dias e nenhum projeto em aberto.");
    }

    @Test
    void projetoEmAbertoImpedeSemTracaoMesmoSemAtividadeRecente() {
        SaudeEstrategia saude = avaliar(IndicadoresBuilder.saudavel().ultimaAtividade(HOJE.minusDays(200)));

        assertThat(saude.nivel()).isEqualTo(NivelSaude.EM_DIA);
    }

    @Test
    void estrategiaQueAindaNaoComecouNaoEstaSemTracao() {
        SaudeEstrategia saude = avaliar(IndicadoresBuilder.semVinculos().criadaEm(HOJE.minusDays(100))
                .periodo(HOJE.plusDays(10), HOJE.plusMonths(6)));

        assertThat(saude.nivel()).isEqualTo(NivelSaude.EM_DIA);
        assertThat(saude.motivos()).contains("O período começa em 03/11/2026.");
    }

    @Test
    void riscoTemPrioridadeSobreFaltaDeTracao() {
        SaudeEstrategia saude = avaliar(IndicadoresBuilder.semVinculos().criadaEm(HOJE.minusDays(100))
                .periodo(HOJE.minusDays(200), HOJE.minusDays(5)));

        assertThat(saude.nivel()).isEqualTo(NivelSaude.EM_RISCO);
        assertThat(saude.motivos()).hasSize(2);
    }

    @Test
    void semMetasAvisaMasNaoMudaONivel() {
        SaudeEstrategia saude = avaliar(IndicadoresBuilder.saudavel().metas(0));

        assertThat(saude.nivel()).isEqualTo(NivelSaude.EM_DIA);
        assertThat(saude.motivos()).contains("Sem metas definidas: o avanço não pode ser medido.");
        assertThat(saude.proximaAcao()).isEqualTo("Defina metas mensuráveis para acompanhar o avanço.");
    }
}
