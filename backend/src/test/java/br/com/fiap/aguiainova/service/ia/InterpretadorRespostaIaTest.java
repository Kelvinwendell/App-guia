package br.com.fiap.aguiainova.service.ia;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import br.com.fiap.aguiainova.integration.ai.LlmIndisponivelException;
import br.com.fiap.aguiainova.model.AvaliacaoIa;
import br.com.fiap.aguiainova.model.enums.Prioridade;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InterpretadorRespostaIaTest {

    private static final Instant AGORA = Instant.parse("2026-06-15T12:00:00Z");

    private final InterpretadorRespostaIa interpretador = new InterpretadorRespostaIa(JsonMapper.builder().build());

    private Map<String, AvaliacaoIa> avaliacoes(String texto) {
        return interpretador.interpretarAvaliacoes(texto, "GEMINI", AGORA);
    }

    @Test
    void extraiOJsonDeDentroDeMarkdownETextoAoRedor() {
        Map<String, AvaliacaoIa> resultado = avaliacoes("Claro!\n```json\n{\"avaliacoes\":[{\"id\":\"a\",\"score\":80,"
                + "\"alinhamentoEstrategico\":60,\"prioridade\":\"ALTA\",\"justificativa\":\"Boa\","
                + "\"pontosFortes\":[\"x\"],\"riscos\":[\"y\"]}]}\n```\nEspero ter ajudado.");

        assertThat(resultado).containsOnlyKeys("a");
        AvaliacaoIa avaliacao = resultado.get("a");
        assertThat(avaliacao.score()).isEqualTo(80);
        assertThat(avaliacao.alinhamentoEstrategico()).isEqualTo(60);
        assertThat(avaliacao.prioridadeSugerida()).isEqualTo(Prioridade.ALTA);
        assertThat(avaliacao.fonte()).isEqualTo("GEMINI");
        assertThat(avaliacao.avaliadaEm()).isEqualTo(AGORA);
    }

    @Test
    void limitaNotasForaDoIntervaloEUsaAUltimaComoAlinhamentoQuandoAusente() {
        AvaliacaoIa alta = avaliacoes("{\"avaliacoes\":[{\"id\":\"a\",\"score\":999}]}").get("a");
        AvaliacaoIa baixa = avaliacoes("{\"avaliacoes\":[{\"id\":\"a\",\"score\":-40,\"alinhamentoEstrategico\":500}]}")
                .get("a");

        assertThat(alta.score()).isEqualTo(100);
        assertThat(alta.alinhamentoEstrategico()).isEqualTo(100);
        assertThat(baixa.score()).isZero();
        assertThat(baixa.alinhamentoEstrategico()).isEqualTo(100);
    }

    @Test
    void prioridadeInvalidaOuComAcentoEInterpretadaSemFalhar() {
        assertThat(avaliacoes("{\"avaliacoes\":[{\"id\":\"a\",\"score\":50,\"prioridade\":\"média\"}]}").get("a")
                .prioridadeSugerida()).isEqualTo(Prioridade.MEDIA);
        assertThat(avaliacoes("{\"avaliacoes\":[{\"id\":\"a\",\"score\":90,\"prioridade\":\"URGENTISSIMA\"}]}").get("a")
                .prioridadeSugerida()).isEqualTo(Prioridade.ALTA);
        assertThat(avaliacoes("{\"avaliacoes\":[{\"id\":\"a\",\"score\":10}]}").get("a").prioridadeSugerida())
                .isEqualTo(Prioridade.BAIXA);
    }

    @Test
    void ignoraEntradasSemIdOuSemNotaEAparaOsIds() {
        Map<String, AvaliacaoIa> resultado = avaliacoes("{\"avaliacoes\":[{\"score\":50},{\"id\":\"b\"},"
                + "{\"id\":\" c \",\"score\":70}, null]}");

        assertThat(resultado).containsOnlyKeys("c");
    }

    @Test
    void listasSaoLimpasELimitadasETextosTruncados() {
        String itens = "\"1\",\"\",\"2\",\"3\",\"4\",\"5\",\"6\",\"7\"";
        String longo = "x".repeat(900);
        AvaliacaoIa avaliacao = avaliacoes("{\"avaliacoes\":[{\"id\":\"a\",\"score\":50,\"pontosFortes\":[" + itens
                + "],\"justificativa\":\"" + longo + "\"}]}").get("a");

        assertThat(avaliacao.pontosFortes()).containsExactly("1", "2", "3", "4", "5");
        assertThat(avaliacao.riscos()).isEmpty();
        assertThat(avaliacao.justificativa()).hasSize(600).endsWith("…");
    }

    @Test
    void semJustificativaUsaUmTextoPadrao() {
        assertThat(avaliacoes("{\"avaliacoes\":[{\"id\":\"a\",\"score\":50}]}").get("a").justificativa())
                .isEqualTo("Sem justificativa informada pelo modelo.");
    }

    @Test
    void respostaSemJsonOuMalFormadaSinalizaIndisponibilidade() {
        assertThatThrownBy(() -> avaliacoes("Desculpe, não consegui.")).isInstanceOf(LlmIndisponivelException.class);
        assertThatThrownBy(() -> avaliacoes("{\"avaliacoes\": [ {\"id\": ")).isInstanceOf(LlmIndisponivelException.class);
        assertThatThrownBy(() -> avaliacoes("{\"avaliacoes\":[{\"id\":\"a\",\"score\":\"abc\"}]}"))
                .isInstanceOf(LlmIndisponivelException.class);
    }

    @Test
    void semAvaliacoesNaRespostaDevolveMapaVazio() {
        assertThat(avaliacoes("{\"outra\":1}")).isEmpty();
    }

    @Test
    void interpretaInsights() {
        InsightsGerados insights = interpretador.interpretarInsights("{\"resumo\":\"Bom\",\"insights\":[\"a\",\"b\"],"
                + "\"recomendacoes\":[\"c\"],\"extra\":true}", "OPENAI");

        assertThat(insights.resumo()).isEqualTo("Bom");
        assertThat(insights.insights()).containsExactly("a", "b");
        assertThat(insights.recomendacoes()).containsExactly("c");
        assertThat(insights.fonte()).isEqualTo("OPENAI");
    }

    @Test
    void insightsVaziosNaoSaoAceitos() {
        assertThatThrownBy(() -> interpretador.interpretarInsights("{\"resumo\":\" \",\"insights\":[]}", "GEMINI"))
                .isInstanceOf(LlmIndisponivelException.class);
    }

    @Test
    void interpretaORascunhoDeEstrategiaDentroDeMarkdown() {
        RascunhoGerado rascunho = interpretador.interpretarRascunho("Aqui está:\n```json\n{\"titulo\":\" Fábrica 4.0 \","
                + "\"descricao\":\"Digitalizar a fábrica.\",\"categoria\":\"Tecnologia\",\"campanha\":\"F40\","
                + "\"metas\":[\"Reduzir paradas em 20%\",\"\",\"Monitorar 100%\"],\"inicio\":\"2026-11-01\","
                + "\"fim\":\"2027-04-30\",\"extra\":1}\n```", "GEMINI");

        assertThat(rascunho.titulo()).isEqualTo("Fábrica 4.0");
        assertThat(rascunho.categoria()).isEqualTo("Tecnologia");
        assertThat(rascunho.campanha()).isEqualTo("F40");
        assertThat(rascunho.metas()).containsExactly("Reduzir paradas em 20%", "Monitorar 100%");
        assertThat(rascunho.inicio()).hasToString("2026-11-01");
        assertThat(rascunho.fim()).hasToString("2027-04-30");
        assertThat(rascunho.fonte()).isEqualTo("GEMINI");
    }

    @Test
    void rascunhoSemTituloOuDescricaoNaoEAceito() {
        assertThatThrownBy(() -> interpretador.interpretarRascunho("{\"titulo\":\"\",\"descricao\":\"x\"}", "GEMINI"))
                .isInstanceOf(LlmIndisponivelException.class);
        assertThatThrownBy(() -> interpretador.interpretarRascunho("{\"titulo\":\"x\"}", "GEMINI"))
                .isInstanceOf(LlmIndisponivelException.class);
    }

    @Test
    void rascunhoUsaPadroesSeguros() {
        RascunhoGerado rascunho = interpretador.interpretarRascunho("{\"titulo\":\"T\",\"descricao\":\"D\","
                + "\"campanha\":\"  \",\"metas\":[\"" + "m".repeat(500) + "\"],\"inicio\":\"amanhã\","
                + "\"fim\":\"2027-04-30\"}", "OPENAI");

        assertThat(rascunho.categoria()).isEqualTo("Geral");
        assertThat(rascunho.campanha()).isNull();
        assertThat(rascunho.inicio()).isNull();
        assertThat(rascunho.fim()).hasToString("2027-04-30");
        assertThat(rascunho.metas().get(0)).hasSize(200);
    }

    @Test
    void rascunhoComFimAntesDoInicioDescartaOPeriodo() {
        RascunhoGerado rascunho = interpretador.interpretarRascunho("{\"titulo\":\"T\",\"descricao\":\"D\","
                + "\"inicio\":\"2027-05-01\",\"fim\":\"2027-01-01\"}", "GEMINI");

        assertThat(rascunho.inicio()).isNull();
        assertThat(rascunho.fim()).isNull();
    }

    @Test
    void rascunhoRespeitaOsLimitesDeTamanhoDaApi() {
        RascunhoGerado rascunho = interpretador.interpretarRascunho("{\"titulo\":\"" + "t".repeat(400)
                + "\",\"descricao\":\"" + "d".repeat(3000) + "\",\"categoria\":\"" + "c".repeat(200) + "\","
                + "\"campanha\":\"" + "p".repeat(300) + "\",\"metas\":[\"1\",\"2\",\"3\",\"4\",\"5\",\"6\",\"7\"]}",
                "GEMINI");

        assertThat(rascunho.titulo()).hasSize(150);
        assertThat(rascunho.descricao()).hasSize(2000);
        assertThat(rascunho.categoria()).hasSize(80);
        assertThat(rascunho.campanha()).hasSize(120);
        assertThat(rascunho.metas()).hasSize(5);
    }

    @Test
    void interpretaAAnaliseLimitandoANotaEFiltrandoSobreposicoesDesconhecidas() {
        AnaliseGerada analise = interpretador.interpretarAnalise("{\"nota\":170,\"resumo\":\"Boa\","
                + "\"pontosFortes\":[\"Clara\"],\"melhorias\":[\"Metas\",\"\"],"
                + "\"sobreposicoes\":[\"e-1\",\" e-1 \",\"inventado\",null]}", "GEMINI", Set.of("e-1", "e-2"));

        assertThat(analise.nota()).isEqualTo(100);
        assertThat(analise.resumo()).isEqualTo("Boa");
        assertThat(analise.pontosFortes()).containsExactly("Clara");
        assertThat(analise.melhorias()).containsExactly("Metas");
        assertThat(analise.sobreposicoes()).containsExactly("e-1");
        assertThat(analise.fonte()).isEqualTo("GEMINI");
    }

    @Test
    void analiseSemNotaNaoEAceitaESemResumoUsaTextoPadrao() {
        assertThatThrownBy(() -> interpretador.interpretarAnalise("{\"resumo\":\"x\"}", "GEMINI", Set.of()))
                .isInstanceOf(LlmIndisponivelException.class);
        assertThat(interpretador.interpretarAnalise("{\"nota\":50}", "GEMINI", Set.of()).resumo())
                .isEqualTo("Sem resumo informado pelo modelo.");
    }

    @Test
    void interpretaALeituraDaSaude() {
        LeituraGerada leitura = interpretador.interpretarLeitura("{\"leitura\":\" Em risco por atraso. \","
                + "\"recomendacoes\":[\"Renegociar o prazo\",\"\"]}", "OPENAI");

        assertThat(leitura.leitura()).isEqualTo("Em risco por atraso.");
        assertThat(leitura.recomendacoes()).containsExactly("Renegociar o prazo");
        assertThat(leitura.fonte()).isEqualTo("OPENAI");
    }

    @Test
    void leituraVaziaNaoEAceita() {
        assertThatThrownBy(() -> interpretador.interpretarLeitura("{\"leitura\":\" \",\"recomendacoes\":[\"a\"]}", "GEMINI"))
                .isInstanceOf(LlmIndisponivelException.class);
    }
}
