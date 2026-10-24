package br.com.fiap.aguiainova.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import br.com.fiap.aguiainova.model.Usuario;
import br.com.fiap.aguiainova.model.enums.Perfil;
import br.com.fiap.aguiainova.model.enums.StatusProjeto;
import br.com.fiap.aguiainova.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EstrategiaControllerIT extends AbstractIntegrationTest {

    private Usuario lideranca;

    @BeforeEach
    void preparar() {
        lideranca = criarUsuario(Perfil.LIDERANCA);
    }

    private Map<String, Object> corpo(String titulo, String categoria, String campanha) {
        return Map.of("titulo", titulo, "descricao", "Descrição de " + titulo, "categoria", categoria,
                "campanha", campanha);
    }

    @Test
    void liderancaCriaEstrategiaVigenteRegistrandoAutoria() throws Exception {
        post(lideranca, "/api/estrategias", corpo("Reduzir custos", "Financeiro", "Campanha 2026"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("VIGENTE"))
                .andExpect(jsonPath("$.autorId").value(lideranca.getId()))
                .andExpect(jsonPath("$.autorNome").value(lideranca.getNome()))
                .andExpect(jsonPath("$.criadoEm").isNotEmpty());
    }

    @Test
    void todosOsPerfisConsultamAsEstrategias() throws Exception {
        cenarios.estrategia(logado(lideranca), "Estratégia A", "Geral", "C1");
        for (Perfil perfil : Perfil.values()) {
            get(criarUsuario(perfil), "/api/estrategias")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].titulo").value("Estratégia A"));
        }
    }

    @Test
    void validaCamposObrigatorios() throws Exception {
        post(lideranca, "/api/estrategias", Map.of("titulo", " ", "descricao", "", "categoria", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.titulo").exists())
                .andExpect(jsonPath("$.errors.descricao").exists())
                .andExpect(jsonPath("$.errors.categoria").exists());
    }

    @Test
    void atualizaEncerraEConsultaPorId() throws Exception {
        String id = campo(post(lideranca, "/api/estrategias", corpo("Original", "Geral", "C1")), "$.id");

        put(lideranca, "/api/estrategias/" + id, Map.of("titulo", "Revisada", "descricao", "Nova descrição",
                "categoria", "Geral", "status", "ENCERRADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Revisada"))
                .andExpect(jsonPath("$.status").value("ENCERRADA"));

        get(lideranca, "/api/estrategias/" + id).andExpect(jsonPath("$.titulo").value("Revisada"));
        get(lideranca, "/api/estrategias?status=VIGENTE").andExpect(jsonPath("$", empty()));
        get(lideranca, "/api/estrategias?status=ENCERRADA").andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void estrategiaInexistenteDa404() throws Exception {
        get(lideranca, "/api/estrategias/000000000000000000000000").andExpect(status().isNotFound());
    }

    @Test
    void cadaMudancaGeraUmRegistroNoHistoricoMesmoDepoisDeRemovida() throws Exception {
        String id = campo(post(lideranca, "/api/estrategias", corpo("Ciclo de vida", "Geral", "C1")), "$.id");
        put(lideranca, "/api/estrategias/" + id, corpo("Ciclo de vida v2", "Geral", "C2")).andExpect(status().isOk());
        delete(lideranca, "/api/estrategias/" + id).andExpect(status().isNoContent());

        get(lideranca, "/api/estrategias/" + id).andExpect(status().isNotFound());
        get(criarUsuario(Perfil.OPERADOR), "/api/estrategias/" + id + "/historico")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].acao").value(containsInAnyOrder("CRIADA", "ATUALIZADA", "REMOVIDA")))
                .andExpect(jsonPath("$[*].estrategiaId").value(containsInAnyOrder(id, id, id)))
                .andExpect(jsonPath("$[0].data").isNotEmpty())
                .andExpect(jsonPath("$[0].categoria").value("Geral"))
                .andExpect(jsonPath("$[0].autorNome").value(lideranca.getNome()));
    }

    @Test
    void historicoPodeSerFiltradoPorCategoriaCampanhaEPeriodo() throws Exception {
        cenarios.estrategia(logado(lideranca), "Sustentável", "Sustentabilidade", "Zero Desperdício");
        cenarios.estrategia(logado(lideranca), "Digital", "Tecnologia", "Fábrica 4.0");
        String hoje = LocalDate.now(java.time.ZoneId.of("America/Sao_Paulo")).toString();

        get(lideranca, "/api/estrategias/historico").andExpect(jsonPath("$", hasSize(2)));
        get(lideranca, "/api/estrategias/historico?categoria=sustentabilidade")
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titulo").value("Sustentável"));
        get(lideranca, "/api/estrategias/historico?campanha=FÁBRICA 4.0")
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titulo").value("Digital"));
        get(lideranca, "/api/estrategias/historico?de=" + hoje + "&ate=" + hoje).andExpect(jsonPath("$", hasSize(2)));
        get(lideranca, "/api/estrategias/historico?ate=2000-01-01").andExpect(jsonPath("$", empty()));
        get(lideranca, "/api/estrategias/historico?de=2999-01-01").andExpect(jsonPath("$", empty()));
    }

    @Test
    void filtrosNaoTratamOTextoComoExpressaoRegular() throws Exception {
        cenarios.estrategia(logado(lideranca), "Qualquer", "Geral", "C.*");

        get(lideranca, "/api/estrategias?campanha=C.*").andExpect(jsonPath("$", hasSize(1)));
        get(lideranca, "/api/estrategias?campanha=.*").andExpect(jsonPath("$", empty()));
    }

    @Test
    void naoRemoveEstrategiaComIdeiaOuProjetoVinculado() throws Exception {
        String estrategia = cenarios.estrategia(logado(lideranca), "Com vínculo", "Geral", "C1");
        cenarios.ideia(logado(criarUsuario(Perfil.OPERADOR)), "Ideia vinculada", estrategia);

        ResultActions resposta = delete(lideranca, "/api/estrategias/" + estrategia);

        resposta.andExpect(status().isConflict());
        get(lideranca, "/api/estrategias/" + estrategia).andExpect(status().isOk());
    }

    @Test
    void salvaMetasEPeriodoELimpaQuandoAtualizadaSemEles() throws Exception {
        String id = campo(post(lideranca, "/api/estrategias", Map.of("titulo", "Com metas", "descricao", "Descrição",
                "categoria", "Geral", "metas", List.of("Reduzir 10%", "  ", "Dobrar as vendas"),
                "inicio", "2026-01-01", "fim", "2026-12-31")), "$.id");

        get(lideranca, "/api/estrategias/" + id)
                .andExpect(jsonPath("$.metas", hasSize(2)))
                .andExpect(jsonPath("$.metas[0]").value("Reduzir 10%"))
                .andExpect(jsonPath("$.metas[1]").value("Dobrar as vendas"))
                .andExpect(jsonPath("$.inicio").value("2026-01-01"))
                .andExpect(jsonPath("$.fim").value("2026-12-31"));

        put(lideranca, "/api/estrategias/" + id, Map.of("titulo", "Com metas", "descricao", "Descrição",
                "categoria", "Geral"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metas", empty()))
                .andExpect(jsonPath("$.inicio").value(nullValue()));
    }

    @Test
    void periodoComFimAntesDoInicioDa422() throws Exception {
        post(lideranca, "/api/estrategias", Map.of("titulo", "Período ruim", "descricao", "Descrição",
                "categoria", "Geral", "inicio", "2026-06-01", "fim", "2026-01-01"))
                .andExpect(status().is(422));
    }

    @Test
    void metasDemaisOuLongasDemaisDao400() throws Exception {
        List<String> nove = IntStream.rangeClosed(1, 9).mapToObj(i -> "Meta " + i).toList();

        post(lideranca, "/api/estrategias", Map.of("titulo", "Muitas", "descricao", "d", "categoria", "c",
                "metas", nove)).andExpect(status().isBadRequest());
        post(lideranca, "/api/estrategias", Map.of("titulo", "Longa", "descricao", "d", "categoria", "c",
                "metas", List.of("x".repeat(201)))).andExpect(status().isBadRequest());
    }

    @Test
    void saudeDeEstrategiaRecemCriadaESemVinculosEstaEmDia() throws Exception {
        String id = cenarios.estrategia(logado(lideranca), "Nova", "Geral", "C1");

        get(criarUsuario(Perfil.GESTOR), "/api/estrategias/" + id + "/saude")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estrategiaId").value(id))
                .andExpect(jsonPath("$.titulo").value("Nova"))
                .andExpect(jsonPath("$.nivel").value("EM_DIA"))
                .andExpect(jsonPath("$.ideias").value(0))
                .andExpect(jsonPath("$.projetos").value(0))
                .andExpect(jsonPath("$.ultimaAtividade").value(nullValue()))
                .andExpect(jsonPath("$.motivos").isNotEmpty())
                .andExpect(jsonPath("$.proximaAcao").isNotEmpty());
    }

    @Test
    void saudeMostraProjetoAtrasadoComoRisco() throws Exception {
        Usuario gestor = criarUsuario(Perfil.GESTOR);
        String id = cenarios.estrategia(logado(lideranca), "Em risco", "Geral", "C1");
        cenarios.projeto(logado(gestor), "Atrasado", "1000", LocalDate.now().minusDays(60),
                LocalDate.now().minusDays(1), StatusProjeto.EM_ANDAMENTO, id);

        get(lideranca, "/api/estrategias/" + id + "/saude")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nivel").value("EM_RISCO"))
                .andExpect(jsonPath("$.projetos").value(1))
                .andExpect(jsonPath("$.projetosAtrasados").value(1))
                .andExpect(jsonPath("$.motivos[0]").value("1 projeto(s) com o prazo vencido."))
                .andExpect(jsonPath("$.investimento").isNumber())
                .andExpect(jsonPath("$.ultimaAtividade").isNotEmpty());
    }

    @Test
    void ideiasAprovadasSemProjetoSaoContadasEDeixamDeSerQuandoViramProjeto() throws Exception {
        Usuario gestor = criarUsuario(Perfil.GESTOR);
        Usuario operador = criarUsuario(Perfil.OPERADOR);
        String id = cenarios.estrategia(logado(lideranca), "Com backlog", "Geral", "C1");
        String primeira = cenarios.ideia(logado(operador), "Primeira", id);
        String segunda = cenarios.ideia(logado(operador), "Segunda", id);
        patch(gestor, "/api/ideias/" + primeira + "/avaliacao", Map.of("status", "APROVADA")).andExpect(status().isOk());
        patch(gestor, "/api/ideias/" + segunda + "/avaliacao", Map.of("status", "APROVADA")).andExpect(status().isOk());

        get(gestor, "/api/estrategias/" + id + "/saude")
                .andExpect(jsonPath("$.ideias").value(2))
                .andExpect(jsonPath("$.aprovadasSemProjeto").value(2))
                .andExpect(jsonPath("$.nivel").value("EM_RISCO"));

        // o projeto pode estar em outra estrategia (ou em nenhuma): o que conta e a ideia ja ter virado projeto
        post(gestor, "/api/projetos", Map.of("nome", "Projeto da primeira", "descricao", "Descrição",
                "investimento", 100, "prazo", LocalDate.now().plusDays(90).toString(), "ideiaId", primeira))
                .andExpect(status().isCreated());

        get(gestor, "/api/estrategias/" + id + "/saude")
                .andExpect(jsonPath("$.aprovadasSemProjeto").value(1))
                .andExpect(jsonPath("$.nivel").value("EM_DIA"));
    }

    @Test
    void estrategiaEncerradaNaoTemSaudeAcompanhada() throws Exception {
        String id = cenarios.estrategiaEncerrada(logado(lideranca), "Encerrada");

        get(lideranca, "/api/estrategias/" + id + "/saude").andExpect(status().is(422));
        get(lideranca, "/api/estrategias/000000000000000000000000/saude").andExpect(status().isNotFound());
    }

    @Test
    void listaDeSaudeTrazSoAsVigentes() throws Exception {
        cenarios.estrategia(logado(lideranca), "Vigente A", "Geral", "C1");
        cenarios.estrategia(logado(lideranca), "Vigente B", "Geral", "C2");
        cenarios.estrategiaEncerrada(logado(lideranca), "Encerrada");

        get(lideranca, "/api/estrategias/saude")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].titulo").value(containsInAnyOrder("Vigente A", "Vigente B")));
    }
}
