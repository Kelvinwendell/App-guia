package br.com.fiap.aguiainova.controller;

import java.util.Map;

import br.com.fiap.aguiainova.model.Usuario;
import br.com.fiap.aguiainova.model.enums.Perfil;
import br.com.fiap.aguiainova.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IdeiaControllerIT extends AbstractIntegrationTest {

    private Usuario operador;
    private Usuario outroOperador;
    private Usuario gestor;
    private Usuario lideranca;

    @BeforeEach
    void preparar() {
        operador = criarUsuario(Perfil.OPERADOR);
        outroOperador = criarUsuario(Perfil.OPERADOR);
        gestor = criarUsuario(Perfil.GESTOR);
        lideranca = criarUsuario(Perfil.LIDERANCA);
    }

    private Map<String, Object> corpo(String titulo) {
        return Map.of("titulo", titulo, "descricao", "Descrição de " + titulo, "categoria", "Tecnologia");
    }

    @Test
    void operadorCadastraIdeiaQueNasceEmAnaliseComPrioridadeMedia() throws Exception {
        post(operador, "/api/ideias", corpo("Sensor de temperatura"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("EM_ANALISE"))
                .andExpect(jsonPath("$.prioridade").value("MEDIA"))
                .andExpect(jsonPath("$.autorId").value(operador.getId()))
                .andExpect(jsonPath("$.autorNome").value(operador.getNome()))
                .andExpect(jsonPath("$.avaliacaoIa").value(nullValue()));
    }

    @Test
    void validaCamposObrigatorios() throws Exception {
        post(operador, "/api/ideias", Map.of("titulo", "", "descricao", "x")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.titulo").exists())
                .andExpect(jsonPath("$.errors.categoria").exists());
    }

    @Test
    void operadorVeApenasAsPropriasIdeiasEGestorVeTodas() throws Exception {
        cenarios.ideia(logado(operador), "Minha ideia", null);
        cenarios.ideia(logado(outroOperador), "Ideia de outro", null);

        get(operador, "/api/ideias").andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titulo").value("Minha ideia"));
        get(outroOperador, "/api/ideias").andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titulo").value("Ideia de outro"));
        get(gestor, "/api/ideias").andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void operadorNaoAcessaNemAlteraIdeiaDeOutroAutor() throws Exception {
        String ideiaAlheia = cenarios.ideia(logado(outroOperador), "Ideia de outro", null);

        get(operador, "/api/ideias/" + ideiaAlheia).andExpect(status().isForbidden());
        put(operador, "/api/ideias/" + ideiaAlheia, corpo("Sequestrada")).andExpect(status().isForbidden());
        delete(operador, "/api/ideias/" + ideiaAlheia).andExpect(status().isForbidden());
        get(gestor, "/api/ideias/" + ideiaAlheia).andExpect(status().isOk());
    }

    @Test
    void ideiaInexistenteDa404() throws Exception {
        get(operador, "/api/ideias/000000000000000000000000").andExpect(status().isNotFound());
    }

    @Test
    void autorEditaEExcluiEnquantoAIdeiaEstaEmAnalise() throws Exception {
        String id = campo(post(operador, "/api/ideias", corpo("Rascunho")), "$.id");

        put(operador, "/api/ideias/" + id, corpo("Versão final"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Versão final"));
        delete(operador, "/api/ideias/" + id).andExpect(status().isNoContent());
        get(operador, "/api/ideias/" + id).andExpect(status().isNotFound());
    }

    @Test
    void depoisDeAvaliadaAIdeiaFicaProtegidaContraEdicaoEExclusao() throws Exception {
        String id = cenarios.ideia(logado(operador), "Ideia avaliada", null);
        patch(gestor, "/api/ideias/" + id + "/avaliacao", Map.of("status", "APROVADA")).andExpect(status().isOk());

        put(operador, "/api/ideias/" + id, corpo("Tentativa")).andExpect(status().isConflict());
        delete(operador, "/api/ideias/" + id).andExpect(status().isConflict());
    }

    @Test
    void gestorAprovaPriorizaEComentaEOAutorVeOParecer() throws Exception {
        String id = cenarios.ideia(logado(operador), "Boa ideia", null);

        patch(gestor, "/api/ideias/" + id + "/avaliacao",
                Map.of("status", "APROVADA", "prioridade", "ALTA", "parecer", "  Ótima proposta  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADA"))
                .andExpect(jsonPath("$.prioridade").value("ALTA"))
                .andExpect(jsonPath("$.parecer").value("Ótima proposta"))
                .andExpect(jsonPath("$.avaliadorNome").value(gestor.getNome()))
                .andExpect(jsonPath("$.avaliadaEm").isNotEmpty());

        get(operador, "/api/ideias/" + id)
                .andExpect(jsonPath("$.status").value("APROVADA"))
                .andExpect(jsonPath("$.parecer").value("Ótima proposta"));
    }

    @Test
    void gestorPodeSoPriorizarSemMudarOStatus() throws Exception {
        String id = cenarios.ideia(logado(operador), "Ideia", null);

        patch(gestor, "/api/ideias/" + id + "/avaliacao", Map.of("prioridade", "ALTA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ANALISE"))
                .andExpect(jsonPath("$.prioridade").value("ALTA"));
    }

    @Test
    void avaliacaoSemNenhumCampoEhRecusada() throws Exception {
        String id = cenarios.ideia(logado(operador), "Ideia", null);

        patch(gestor, "/api/ideias/" + id + "/avaliacao", Map.of()).andExpect(status().isBadRequest());
    }

    @Test
    void vinculaAIdeiaSomenteAUmaEstrategiaVigente() throws Exception {
        String vigente = cenarios.estrategia(logado(lideranca), "Vigente", "Geral", "C1");
        String encerrada = cenarios.estrategiaEncerrada(logado(lideranca), "Encerrada");

        post(operador, "/api/ideias", Map.of("titulo", "Ligada", "descricao", "Descrição", "categoria", "Geral",
                "estrategiaId", vigente))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estrategiaId").value(vigente))
                .andExpect(jsonPath("$.estrategiaTitulo").value("Vigente"));
        post(operador, "/api/ideias", Map.of("titulo", "Ligada", "descricao", "Descrição", "categoria", "Geral",
                "estrategiaId", encerrada)).andExpect(status().isUnprocessableContent());
        post(operador, "/api/ideias", Map.of("titulo", "Ligada", "descricao", "Descrição", "categoria", "Geral",
                "estrategiaId", "000000000000000000000000")).andExpect(status().isUnprocessableContent());
    }

    @Test
    void gestorFiltraPorStatusPrioridadeECategoria() throws Exception {
        String a = cenarios.ideia(logado(operador), "A", null);
        cenarios.ideia(logado(operador), "B", null);
        patch(gestor, "/api/ideias/" + a + "/avaliacao", Map.of("status", "APROVADA", "prioridade", "ALTA"));

        get(gestor, "/api/ideias?status=APROVADA").andExpect(jsonPath("$", hasSize(1)));
        get(gestor, "/api/ideias?status=EM_ANALISE").andExpect(jsonPath("$", hasSize(1)));
        get(gestor, "/api/ideias?prioridade=ALTA").andExpect(jsonPath("$", hasSize(1)));
        get(gestor, "/api/ideias?categoria=tecnologia").andExpect(jsonPath("$", hasSize(2)));
        get(gestor, "/api/ideias?status=INVALIDO").andExpect(status().isBadRequest());
    }

    @Test
    void ideiasAprovadasSaoVisiveisATodosOsPerfisSemExporANotaDaIa() throws Exception {
        String id = cenarios.ideiaAprovada(logado(outroOperador), logado(gestor), "Aprovada de outro");
        cenarios.ideia(logado(outroOperador), "Ainda em análise", null);
        post(gestor, "/api/ia/ideias/" + id + "/avaliar", null).andExpect(status().isOk());

        get(operador, "/api/ideias/aprovadas")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titulo").value("Aprovada de outro"))
                .andExpect(jsonPath("$[0].avaliacaoIa").value(nullValue()));
        get(gestor, "/api/ideias/aprovadas").andExpect(jsonPath("$[0].avaliacaoIa.score").isNumber());
        get(lideranca, "/api/ideias/aprovadas").andExpect(status().isOk());
    }
}
