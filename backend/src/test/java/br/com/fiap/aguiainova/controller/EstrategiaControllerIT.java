package br.com.fiap.aguiainova.controller;

import java.time.LocalDate;
import java.util.Map;

import br.com.fiap.aguiainova.model.Usuario;
import br.com.fiap.aguiainova.model.enums.Perfil;
import br.com.fiap.aguiainova.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
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
}
