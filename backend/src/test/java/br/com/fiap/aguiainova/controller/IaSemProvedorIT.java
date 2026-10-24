package br.com.fiap.aguiainova.controller;

import java.time.LocalDate;
import java.util.Map;

import br.com.fiap.aguiainova.model.Estrategia;
import br.com.fiap.aguiainova.model.Usuario;
import br.com.fiap.aguiainova.model.enums.Perfil;
import br.com.fiap.aguiainova.model.enums.StatusProjeto;
import br.com.fiap.aguiainova.support.AbstractIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.query.Query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Sem chave de API o sistema continua util: a pontuacao e os insights caem nas regras locais (HEURISTICA). */
class IaSemProvedorIT extends AbstractIntegrationTest {

    private static final String DESCRICAO_ALINHADA = "Reduzir o desperdício de material na produção separando as "
            + "sobras de corte, reaproveitando cada sobra em peças menores e medindo o desperdício por turno para "
            + "que a equipe de produção acompanhe a redução semana a semana e evite o descarte de material útil.";

    private Usuario operador;
    private Usuario gestor;
    private Usuario lideranca;

    @BeforeEach
    void preparar() {
        operador = criarUsuario(Perfil.OPERADOR);
        gestor = criarUsuario(Perfil.GESTOR);
        lideranca = criarUsuario(Perfil.LIDERANCA);
        cenarios.estrategia(logado(lideranca), "Redução de desperdício na produção", "Sustentabilidade",
                "Zero Desperdício");
    }

    private String ideiaAlinhada() throws Exception {
        return campo(post(operador, "/api/ideias", Map.of("titulo", "Reduzir desperdício de material na produção",
                "descricao", DESCRICAO_ALINHADA, "categoria", "Sustentabilidade")), "$.id");
    }

    private String ideiaSemRelacao() throws Exception {
        return campo(post(operador, "/api/ideias", Map.of("titulo", "Cadeira gamer", "descricao", "Comprar cadeira",
                "categoria", "Outros")), "$.id");
    }

    @Test
    void statusInformaQueNaoHaProvedorConfigurado() throws Exception {
        get(operador, "/api/ia/status")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponivel").value(false))
                .andExpect(jsonPath("$.provedor").value("NENHUM"));
    }

    @Test
    void pontuaUmaIdeiaPorRegrasEPersisteAAvaliacao() throws Exception {
        String id = ideiaAlinhada();

        post(gestor, "/api/ia/ideias/" + id + "/avaliar", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ideiaId").value(id))
                .andExpect(jsonPath("$.avaliacao.fonte").value("HEURISTICA"))
                .andExpect(jsonPath("$.avaliacao.score").value(greaterThan(50)))
                .andExpect(jsonPath("$.avaliacao.score").value(lessThanOrEqualTo(100)))
                .andExpect(jsonPath("$.avaliacao.prioridadeSugerida").isNotEmpty())
                .andExpect(jsonPath("$.avaliacao.justificativa").value(containsString("Avaliação automática")));

        get(gestor, "/api/ideias/" + id).andExpect(jsonPath("$.avaliacaoIa.score").isNumber());
        get(operador, "/api/ideias/" + id).andExpect(jsonPath("$.avaliacaoIa").value(nullValue()));
    }

    @Test
    void ideiaAlinhadaAEstrategiaRecebeNotaMaiorQueUmaSemRelacao() throws Exception {
        String alinhada = ideiaAlinhada();
        String semRelacao = ideiaSemRelacao();

        post(gestor, "/api/ia/ideias/avaliar-pendentes", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ranking", hasSize(2)))
                .andExpect(jsonPath("$.ranking[0].ideiaId").value(alinhada))
                .andExpect(jsonPath("$.ranking[1].ideiaId").value(semRelacao));

        assertThat(scoreDe(alinhada)).isGreaterThan(scoreDe(semRelacao));
    }

    private int scoreDe(String ideiaId) throws Exception {
        String corpo = get(gestor, "/api/ideias/" + ideiaId).andReturn().getResponse().getContentAsString();
        return JsonPath.read(corpo, "$.avaliacaoIa.score");
    }

    @Test
    void avaliarPendentesSoPontuaQuemAindaNaoTemNota() throws Exception {
        ideiaAlinhada();
        ideiaSemRelacao();

        post(gestor, "/api/ia/ideias/avaliar-pendentes", null).andExpect(jsonPath("$.avaliadasAgora").value(2));
        post(gestor, "/api/ia/ideias/avaliar-pendentes", null)
                .andExpect(jsonPath("$.avaliadasAgora").value(0))
                .andExpect(jsonPath("$.ranking", hasSize(2)));
    }

    @Test
    void ideiaJaAprovadaSaiDoRanking() throws Exception {
        String id = ideiaAlinhada();
        ideiaSemRelacao();
        post(gestor, "/api/ia/ideias/avaliar-pendentes", null).andExpect(jsonPath("$.ranking", hasSize(2)));

        patch(gestor, "/api/ideias/" + id + "/avaliacao", Map.of("status", "APROVADA")).andExpect(status().isOk());

        post(gestor, "/api/ia/ideias/avaliar-pendentes", null).andExpect(jsonPath("$.ranking", hasSize(1)));
    }

    @Test
    void editarAIdeiaDescartaANotaQueValiaParaOTextoAntigo() throws Exception {
        String id = ideiaAlinhada();
        post(gestor, "/api/ia/ideias/" + id + "/avaliar", null).andExpect(status().isOk());

        put(operador, "/api/ideias/" + id, Map.of("titulo", "Texto novo", "descricao", "Outra descrição",
                "categoria", "Outros")).andExpect(status().isOk());

        get(gestor, "/api/ideias/" + id).andExpect(jsonPath("$.avaliacaoIa").value(nullValue()));
    }

    @Test
    void ideiaInexistenteDa404() throws Exception {
        post(gestor, "/api/ia/ideias/000000000000000000000000/avaliar", null).andExpect(status().isNotFound());
    }

    @Test
    void insightsUsamLeituraPorRegrasComOsNumerosReais() throws Exception {
        String estrategia = cenarios.estrategia(logado(lideranca), "Meta digital", "Tecnologia", "C1");
        String projeto = cenarios.projeto(logado(gestor), "Projeto X", "1000", LocalDate.now().plusDays(30),
                StatusProjeto.EM_ANDAMENTO, estrategia);
        cenarios.resultado(projeto, logado(gestor), LocalDate.now(), "1500", 8.0, 3.0);

        post(lideranca, "/api/ia/dashboard/insights", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fonte").value("HEURISTICA"))
                .andExpect(jsonPath("$.resumo").value(startsWith("Panorama:")))
                .andExpect(jsonPath("$.insights[0]").value(containsString("ROI geral é positivo")))
                .andExpect(jsonPath("$.recomendacoes").isNotEmpty())
                .andExpect(jsonPath("$.geradoEm").isNotEmpty());
    }

    @Test
    void insightsSemDadosAvisamQueFaltamInformacoes() throws Exception {
        mongo.remove(new Query(), "estrategias");

        post(lideranca, "/api/ia/dashboard/insights", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.insights[0]").value(containsString("Ainda não há dados suficientes")));
    }

    @Test
    void chatNaoTemSubstitutoESemProvedorResponde503() throws Exception {
        post(gestor, "/api/ia/chat", Map.of("mensagem", "Quais ideias priorizar?"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.detail").value(containsString("não está configurado")));
    }

    @Test
    void chatValidaAMensagem() throws Exception {
        post(gestor, "/api/ia/chat", Map.of("mensagem", " ")).andExpect(status().isBadRequest());
    }

    private String estrategiaDaPreparacao() {
        return mongo.findAll(Estrategia.class).get(0).getId();
    }

    @Test
    void rascunhoDeEstrategiaNaoTemSubstitutoESemProvedorResponde503() throws Exception {
        post(lideranca, "/api/ia/estrategias/rascunho", Map.of("objetivo", "Reduzir o desperdício"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.detail").value(containsString("não está configurada")));
        post(lideranca, "/api/ia/estrategias/rascunho", Map.of("objetivo", " ")).andExpect(status().isBadRequest());
    }

    @Test
    void analiseDeEstrategiaSemProvedorUsaRegrasEApontaLacunasESobreposicao() throws Exception {
        post(lideranca, "/api/ia/estrategias/analise", Map.of("estrategia", Map.of(
                "titulo", "Reduzir desperdício de material",
                "descricao", "Reduzir desperdício de material na produção",
                "categoria", "Sustentabilidade")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fonte").value("HEURISTICA"))
                .andExpect(jsonPath("$.nota").value(lessThanOrEqualTo(100)))
                .andExpect(jsonPath("$.melhorias").isNotEmpty())
                .andExpect(jsonPath("$.sobreposicoes", hasSize(1)))
                .andExpect(jsonPath("$.sobreposicoes[0].titulo").value("Redução de desperdício na produção"))
                .andExpect(jsonPath("$.geradaEm").isNotEmpty());
    }

    @Test
    void analiseDeUmaEstrategiaJaSalvaNaoACompararComSiMesma() throws Exception {
        post(lideranca, "/api/ia/estrategias/analise", Map.of("estrategiaId", estrategiaDaPreparacao(),
                "estrategia", Map.of("titulo", "Redução de desperdício na produção",
                        "descricao", "Descrição de Redução de desperdício na produção", "categoria", "Sustentabilidade")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sobreposicoes", hasSize(0)));
    }

    @Test
    void analiseValidaOsCamposDaEstrategia() throws Exception {
        post(lideranca, "/api/ia/estrategias/analise", Map.of("estrategia", Map.of("titulo", " ", "descricao", "d",
                "categoria", "c"))).andExpect(status().isBadRequest());
        post(lideranca, "/api/ia/estrategias/analise", Map.of()).andExpect(status().isBadRequest());
    }

    @Test
    void leituraDaSaudeSemProvedorReaproveitaAsRegras() throws Exception {
        post(gestor, "/api/ia/estrategias/" + estrategiaDaPreparacao() + "/saude", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fonte").value("HEURISTICA"))
                .andExpect(jsonPath("$.saude.nivel").value("EM_DIA"))
                .andExpect(jsonPath("$.leitura").value(containsString("está em dia")))
                .andExpect(jsonPath("$.recomendacoes").isNotEmpty())
                .andExpect(jsonPath("$.geradaEm").isNotEmpty());
    }

    @Test
    void leituraDaSaudeDeEstrategiaInexistenteOuEncerradaFalha() throws Exception {
        String encerrada = cenarios.estrategiaEncerrada(logado(lideranca), "Encerrada");

        post(gestor, "/api/ia/estrategias/000000000000000000000000/saude", null).andExpect(status().isNotFound());
        post(gestor, "/api/ia/estrategias/" + encerrada + "/saude", null).andExpect(status().is(422));
    }
}
