package br.com.fiap.aguiainova.service.ia;

import br.com.fiap.aguiainova.model.DiagnosticoEstrategia;
import br.com.fiap.aguiainova.model.IndicadoresEstrategia;
import br.com.fiap.aguiainova.model.SaudeEstrategia;
import br.com.fiap.aguiainova.support.Dados;
import br.com.fiap.aguiainova.support.IndicadoresBuilder;
import org.junit.jupiter.api.Test;

import static br.com.fiap.aguiainova.support.IndicadoresBuilder.HOJE;
import static org.assertj.core.api.Assertions.assertThat;

class LeitorSaudeHeuristicoTest {

    private final LeitorSaudeHeuristico leitor = new LeitorSaudeHeuristico();

    private static DiagnosticoEstrategia diagnostico(IndicadoresEstrategia indicadores) {
        return new DiagnosticoEstrategia(
                Dados.estrategia("e-1", "Fábrica 4.0", "Descrição", "Tecnologia", null), indicadores,
                SaudeEstrategia.avaliar(indicadores, HOJE));
    }

    @Test
    void reaproveitaOsMotivosEAProximaAcaoDasRegras() {
        LeituraGerada leitura = leitor.ler(diagnostico(IndicadoresBuilder.saudavel().projetos(2, 2, 1, 0).build()));

        assertThat(leitura.leitura()).isEqualTo("A estratégia \"Fábrica 4.0\" exige atenção. "
                + "1 projeto(s) com o prazo vencido.");
        assertThat(leitura.recomendacoes()).containsExactly(
                "Reveja o prazo ou reforce a equipe dos projetos atrasados.");
        assertThat(leitura.fonte()).isEqualTo(FonteIa.HEURISTICA);
    }

    @Test
    void aberturaMudaComONivel() {
        assertThat(leitor.ler(diagnostico(IndicadoresBuilder.saudavel().build())).leitura())
                .startsWith("A estratégia \"Fábrica 4.0\" está em dia.");
        assertThat(leitor.ler(diagnostico(IndicadoresBuilder.semVinculos().criadaEm(HOJE.minusDays(30)).build()))
                .leitura()).startsWith("A estratégia \"Fábrica 4.0\" ainda não ganhou tração.");
    }
}
