package br.com.fiap.aguiainova.service.ia;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import br.com.fiap.aguiainova.model.AvaliacaoIa;
import br.com.fiap.aguiainova.model.Estrategia;
import br.com.fiap.aguiainova.model.Ideia;
import br.com.fiap.aguiainova.model.enums.Prioridade;
import br.com.fiap.aguiainova.support.Dados;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AvaliadorHeuristicoTest {

    private static final Instant AGORA = Instant.parse("2026-06-15T12:00:00Z");
    private static final String DESCRICAO_LONGA = "Reduzir o desperdício de material na produção separando as sobras de "
            + "corte, reaproveitando cada sobra em peças menores e medindo o desperdício por turno para que a equipe "
            + "de produção acompanhe a redução semana a semana e evite o descarte de material útil.";

    private final AvaliadorHeuristico avaliador = new AvaliadorHeuristico(Clock.fixed(AGORA, ZoneOffset.UTC));

    private final Estrategia estrategia = Dados.estrategia("e-1", "Redução de desperdício na produção",
            "Reduzir perdas de material e retrabalho", "Sustentabilidade", "Zero Desperdício");

    private AvaliacaoIa avaliar(Ideia ideia, Estrategia... estrategias) {
        return avaliador.avaliar(List.of(ideia), List.of(estrategias)).get(ideia.getId());
    }

    @Test
    void ideiaAlinhadaEDetalhadaRecebeNotaAltaEPrioridadeAlta() {
        Ideia ideia = Dados.ideia("i-1", "Reduzir desperdício de material na produção", DESCRICAO_LONGA,
                "Sustentabilidade", "e-1");

        AvaliacaoIa avaliacao = avaliar(ideia, estrategia);

        assertThat(avaliacao.score()).isBetween(70, 100);
        assertThat(avaliacao.alinhamentoEstrategico()).isGreaterThanOrEqualTo(50);
        assertThat(avaliacao.prioridadeSugerida()).isEqualTo(Prioridade.ALTA);
        assertThat(avaliacao.fonte()).isEqualTo(FonteIa.HEURISTICA);
        assertThat(avaliacao.avaliadaEm()).isEqualTo(AGORA);
        assertThat(avaliacao.pontosFortes()).contains("Já vinculada a uma estratégia vigente");
    }

    @Test
    void ideiaSemRelacaoEComDescricaoCurtaRecebeNotaBaixaEListaOsRiscos() {
        Ideia ideia = Dados.ideia("i-2", "Cadeira gamer", "Comprar cadeira", "Outros", null);

        AvaliacaoIa avaliacao = avaliar(ideia, estrategia);

        assertThat(avaliacao.score()).isLessThan(20);
        assertThat(avaliacao.prioridadeSugerida()).isEqualTo(Prioridade.BAIXA);
        assertThat(avaliacao.riscos()).contains("Baixo alinhamento com as estratégias vigentes")
                .anyMatch(risco -> risco.startsWith("Descrição curta"));
    }

    @Test
    void semEstrategiasVigentesOAlinhamentoEZeroEORiscoEInformado() {
        Ideia ideia = Dados.ideia("i-3", "Reduzir desperdício de material", DESCRICAO_LONGA, "Sustentabilidade", null);

        AvaliacaoIa avaliacao = avaliar(ideia);

        assertThat(avaliacao.alinhamentoEstrategico()).isZero();
        assertThat(avaliacao.riscos()).contains("Não há estratégias vigentes para comparar");
        assertThat(avaliacao.justificativa()).contains("nenhuma estratégia vigente");
    }

    @Test
    void ignoraAcentosEMaiusculasAoComparar() {
        Ideia comAcento = Dados.ideia("i-4", "REDUÇÃO DE DESPERDÍCIO", DESCRICAO_LONGA, "sustentabilidade", null);
        Ideia semAcento = Dados.ideia("i-5", "reducao de desperdicio", DESCRICAO_LONGA, "SUSTENTABILIDADE", null);

        assertThat(avaliar(comAcento, estrategia).score()).isEqualTo(avaliar(semAcento, estrategia).score());
    }

    @Test
    void oVinculoComEstrategiaSomaPontos() {
        Ideia vinculada = Dados.ideia("i-6", "Ideia genérica sobre algo", "Texto qualquer aqui", "Geral", "e-1");
        Ideia solta = Dados.ideia("i-7", "Ideia genérica sobre algo", "Texto qualquer aqui", "Geral", null);

        assertThat(avaliar(vinculada, estrategia).score()).isEqualTo(avaliar(solta, estrategia).score() + 10);
    }

    @Test
    void ANotaFicaSempreEntreZeroECemEEDeterministica() {
        Ideia ideia = Dados.ideia("i-8", "Reduzir desperdício de material na produção", DESCRICAO_LONGA.repeat(5),
                "Sustentabilidade", "e-1");

        AvaliacaoIa primeira = avaliar(ideia, estrategia);
        AvaliacaoIa segunda = avaliar(ideia, estrategia);

        assertThat(primeira.score()).isBetween(0, 100);
        assertThat(primeira).isEqualTo(segunda);
    }

    @Test
    void avaliaVariasIdeiasIndexandoPeloId() {
        Ideia a = Dados.ideia("a", "Ideia A", "Descrição A", "Geral", null);
        Ideia b = Dados.ideia("b", "Ideia B", "Descrição B", "Geral", null);

        Map<String, AvaliacaoIa> avaliacoes = avaliador.avaliar(List.of(a, b), List.of(estrategia));

        assertThat(avaliacoes).containsOnlyKeys("a", "b");
    }
}
