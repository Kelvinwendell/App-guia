package br.com.fiap.aguiainova.controller;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import br.com.fiap.aguiainova.model.Usuario;
import br.com.fiap.aguiainova.model.enums.Perfil;
import br.com.fiap.aguiainova.model.enums.StatusProjeto;
import br.com.fiap.aguiainova.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProjetoControllerIT extends AbstractIntegrationTest {

    @Autowired
    private Clock clock;

    private Usuario gestor;
    private Usuario lideranca;
    private Usuario operador;
    private LocalDate hoje;

    @BeforeEach
    void preparar() {
        gestor = criarUsuario(Perfil.GESTOR);
        lideranca = criarUsuario(Perfil.LIDERANCA);
        operador = criarUsuario(Perfil.OPERADOR);
        hoje = LocalDate.now(clock);
    }

    private Map<String, Object> corpo(String nome, Object investimento, LocalDate prazo) {
        Map<String, Object> corpo = new HashMap<>();
        corpo.put("nome", nome);
        corpo.put("descricao", "Descrição de " + nome);
        corpo.put("investimento", investimento);
        corpo.put("prazo", prazo.toString());
        return corpo;
    }

    private String criarProjeto(double investimento) throws Exception {
        return campo(post(gestor, "/api/projetos", corpo("Projeto", investimento, hoje.plusDays(60))), "$.id");
    }

    @Test
    void gestorCadastraProjetoComPadroesEIndicadoresIniciais() throws Exception {
        post(gestor, "/api/projetos", corpo("Linha 3", 1234.56, hoje.plusDays(90)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PLANEJAMENTO"))
                .andExpect(jsonPath("$.etapa").value("INICIACAO"))
                .andExpect(jsonPath("$.progresso").value(0))
                .andExpect(jsonPath("$.dataInicio").value(hoje.toString()))
                .andExpect(jsonPath("$.investimento").value(1234.56))
                .andExpect(jsonPath("$.retornoFinanceiro").value(0))
                .andExpect(jsonPath("$.lucro").value(-1234.56))
                .andExpect(jsonPath("$.roi").value(-100.0))
                .andExpect(jsonPath("$.atrasado").value(false))
                .andExpect(jsonPath("$.diasParaPrazo").value(90))
                .andExpect(jsonPath("$.autorId").value(gestor.getId()))
                .andExpect(jsonPath("$.resultados", hasSize(0)));
    }

    @Test
    void investimentoZeroNaoTemRoiEInvestimentoNegativoEhRecusado() throws Exception {
        post(gestor, "/api/projetos", corpo("Sem custo", 0, hoje.plusDays(10)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roi").doesNotExist());
        post(gestor, "/api/projetos", corpo("Negativo", -1, hoje.plusDays(10))).andExpect(status().isBadRequest());
    }

    @Test
    void validaCamposObrigatoriosEPrazoAnteriorAoInicio() throws Exception {
        post(gestor, "/api/projetos", Map.of("nome", "", "descricao", "x")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.nome").exists())
                .andExpect(jsonPath("$.errors.investimento").exists())
                .andExpect(jsonPath("$.errors.prazo").exists());

        Map<String, Object> corpo = corpo("Prazo ruim", 100, hoje.plusDays(5));
        corpo.put("dataInicio", hoje.plusDays(10).toString());
        post(gestor, "/api/projetos", corpo).andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("O prazo não pode ser anterior à data de início."));
    }

    @Test
    void projetoSoNasceDeIdeiaAprovadaEDeEstrategiaVigente() throws Exception {
        String emAnalise = cenarios.ideia(logado(operador), "Ideia crua", null);
        String aprovada = cenarios.ideiaAprovada(logado(operador), logado(gestor), "Ideia aprovada");
        String encerrada = cenarios.estrategiaEncerrada(logado(lideranca), "Antiga");

        Map<String, Object> comIdeiaCrua = corpo("A", 100, hoje.plusDays(10));
        comIdeiaCrua.put("ideiaId", emAnalise);
        post(gestor, "/api/projetos", comIdeiaCrua).andExpect(status().isUnprocessableContent());

        Map<String, Object> comEstrategiaEncerrada = corpo("B", 100, hoje.plusDays(10));
        comEstrategiaEncerrada.put("estrategiaId", encerrada);
        post(gestor, "/api/projetos", comEstrategiaEncerrada).andExpect(status().isUnprocessableContent());

        Map<String, Object> valido = corpo("C", 100, hoje.plusDays(10));
        valido.put("ideiaId", aprovada);
        post(gestor, "/api/projetos", valido)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ideiaId").value(aprovada))
                .andExpect(jsonPath("$.ideiaTitulo").value("Ideia aprovada"));
    }

    @Test
    void atualizaStatusEtapaEProgresso() throws Exception {
        String id = criarProjeto(1000);

        patch(gestor, "/api/projetos/" + id + "/progresso",
                Map.of("status", "EM_ANDAMENTO", "etapa", "EXECUCAO", "progresso", 40))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"))
                .andExpect(jsonPath("$.etapa").value("EXECUCAO"))
                .andExpect(jsonPath("$.progresso").value(40));

        patch(gestor, "/api/projetos/" + id + "/progresso", Map.of("progresso", 55))
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"))
                .andExpect(jsonPath("$.progresso").value(55));
    }

    @Test
    void recusaProgressoForaDeZeroACemEAtualizacaoVazia() throws Exception {
        String id = criarProjeto(1000);

        patch(gestor, "/api/projetos/" + id + "/progresso", Map.of("progresso", 101)).andExpect(status().isBadRequest());
        patch(gestor, "/api/projetos/" + id + "/progresso", Map.of("progresso", -1)).andExpect(status().isBadRequest());
        patch(gestor, "/api/projetos/" + id + "/progresso", Map.of()).andExpect(status().isBadRequest());
    }

    @Test
    void projetoConcluidoFicaSempreComCemPorCentoDeProgresso() throws Exception {
        String id = criarProjeto(1000);

        patch(gestor, "/api/projetos/" + id + "/progresso", Map.of("status", "CONCLUIDO", "progresso", 30))
                .andExpect(jsonPath("$.status").value("CONCLUIDO"))
                .andExpect(jsonPath("$.progresso").value(100));
    }

    @Test
    void resultadosAlimentamRetornoLucroRoiEMediasSemPerderPrecisao() throws Exception {
        String id = criarProjeto(1234.56);

        post(gestor, "/api/projetos/" + id + "/resultados", Map.of("descricao", "Primeiro marco",
                "retornoFinanceiro", 100.10, "ganhoProdutividade", 10, "reducaoCustos", 4, "data", hoje.toString()))
                .andExpect(status().isCreated());
        post(gestor, "/api/projetos/" + id + "/resultados", Map.of("descricao", "Segundo marco",
                "retornoFinanceiro", 200.20, "ganhoProdutividade", 20))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.retornoFinanceiro").value(300.30))
                .andExpect(jsonPath("$.lucro").value(-934.26))
                .andExpect(jsonPath("$.roi").value(-75.68))
                .andExpect(jsonPath("$.ganhoProdutividade").value(15.0))
                .andExpect(jsonPath("$.reducaoCustos").value(4.0))
                .andExpect(jsonPath("$.resultados", hasSize(2)))
                .andExpect(jsonPath("$.resultados[0].id").isNotEmpty())
                .andExpect(jsonPath("$.resultados[0].registradoPorNome").value(gestor.getNome()));
    }

    @Test
    void removeResultadoRecalculandoOsIndicadores() throws Exception {
        String id = criarProjeto(1000);
        String resultadoId = campo(post(gestor, "/api/projetos/" + id + "/resultados",
                Map.of("descricao", "A", "retornoFinanceiro", 400)), "$.resultados[0].id");
        post(gestor, "/api/projetos/" + id + "/resultados", Map.of("descricao", "B", "retornoFinanceiro", 600));

        delete(gestor, "/api/projetos/" + id + "/resultados/" + resultadoId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.retornoFinanceiro").value(600))
                .andExpect(jsonPath("$.resultados", hasSize(1)));
        delete(gestor, "/api/projetos/" + id + "/resultados/inexistente").andExpect(status().isNotFound());
    }

    @Test
    void resultadoNaoAceitaRetornoNegativo() throws Exception {
        String id = criarProjeto(1000);

        post(gestor, "/api/projetos/" + id + "/resultados", Map.of("descricao", "Ruim", "retornoFinanceiro", -5))
                .andExpect(status().isBadRequest());
    }

    @Test
    void atualizaOsDadosMantendoStatusEEtapaQuandoOmitidos() throws Exception {
        String id = criarProjeto(1000);
        patch(gestor, "/api/projetos/" + id + "/progresso", Map.of("status", "EM_ANDAMENTO", "etapa", "EXECUCAO"));

        put(gestor, "/api/projetos/" + id, corpo("Nome revisado", 2000, hoje.plusDays(120)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome revisado"))
                .andExpect(jsonPath("$.investimento").value(2000))
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"))
                .andExpect(jsonPath("$.etapa").value("EXECUCAO"));
    }

    @Test
    void projetoComPrazoVencidoEstaAtrasadoSalvoSeConcluido() throws Exception {
        String id = cenarios.projeto(logado(gestor), "Atrasado", "500", hoje.minusDays(30), hoje.minusDays(1),
                StatusProjeto.EM_ANDAMENTO, null);

        get(lideranca, "/api/projetos/" + id)
                .andExpect(jsonPath("$.atrasado").value(true))
                .andExpect(jsonPath("$.diasParaPrazo").value(-1));

        patch(gestor, "/api/projetos/" + id + "/progresso", Map.of("status", "CONCLUIDO"))
                .andExpect(jsonPath("$.atrasado").value(false));
    }

    @Test
    void liderancaConsultaOAndamentoComTodosOsIndicadores() throws Exception {
        String id = criarProjeto(1000);
        post(gestor, "/api/projetos/" + id + "/resultados", Map.of("descricao", "A", "retornoFinanceiro", 250));

        get(lideranca, "/api/projetos")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].etapa").value("INICIACAO"))
                .andExpect(jsonPath("$[0].status").value("PLANEJAMENTO"))
                .andExpect(jsonPath("$[0].investimento").value(1000))
                .andExpect(jsonPath("$[0].prazo").value(hoje.plusDays(60).toString()))
                .andExpect(jsonPath("$[0].retornoFinanceiro").value(250));
        get(operador, "/api/projetos").andExpect(status().isForbidden());
    }

    @Test
    void listaComFiltrosEPodeExcluir() throws Exception {
        String estrategia = cenarios.estrategia(logado(lideranca), "Estratégia", "Geral", "C1");
        String a = cenarios.projeto(logado(gestor), "A", "100", hoje.plusDays(10), StatusProjeto.EM_ANDAMENTO,
                estrategia);
        cenarios.projeto(logado(gestor), "B", "100", hoje.plusDays(10), StatusProjeto.PLANEJAMENTO, null);

        get(gestor, "/api/projetos?status=EM_ANDAMENTO").andExpect(jsonPath("$", hasSize(1)));
        get(gestor, "/api/projetos?estrategiaId=" + estrategia).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].estrategiaTitulo").value("Estratégia"));
        get(gestor, "/api/projetos?etapa=INICIACAO").andExpect(jsonPath("$", hasSize(2)));

        delete(gestor, "/api/projetos/" + a).andExpect(status().isNoContent());
        get(gestor, "/api/projetos/" + a).andExpect(status().isNotFound());
        get(gestor, "/api/projetos").andExpect(jsonPath("$", hasSize(1)));
    }
}
