package br.com.fiap.aguiainova.controller;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;

import br.com.fiap.aguiainova.model.Usuario;
import br.com.fiap.aguiainova.model.enums.Perfil;
import br.com.fiap.aguiainova.model.enums.StatusProjeto;
import br.com.fiap.aguiainova.security.UsuarioLogado;
import br.com.fiap.aguiainova.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cenario com numeros conhecidos:
 * <pre>
 * P1 (estrategia A) investimento 100.000, retorno 150.000 (50.000 ha 2 meses + 100.000 hoje)  -> lucro  50.000, ROI  50%
 * P2 (estrategia B) investimento  50.000, retorno  20.000 (hoje), prazo vencido               -> lucro -30.000, ROI -60%
 * P3 (estrategia A) CANCELADO, investimento 999.999 (fora dos indicadores financeiros)
 * ideias: 1 aprovada, 1 rejeitada, 1 em analise
 * </pre>
 */
class DashboardControllerIT extends AbstractIntegrationTest {

    @Autowired
    private Clock clock;

    private Usuario lideranca;
    private String projeto1;
    private String projeto2;
    private String estrategiaA;
    private LocalDate hoje;

    @BeforeEach
    void montarCenario() {
        hoje = LocalDate.now(clock);
        lideranca = criarUsuario(Perfil.LIDERANCA);
        UsuarioLogado lider = logado(lideranca);
        UsuarioLogado gestor = logado(criarUsuario(Perfil.GESTOR));
        UsuarioLogado operador = logado(criarUsuario(Perfil.OPERADOR));

        estrategiaA = cenarios.estrategia(lider, "Estratégia A", "Tecnologia", "CA");
        String estrategiaB = cenarios.estrategia(lider, "Estratégia B", "Pessoas", "CB");

        projeto1 = cenarios.projeto(gestor, "P1", "100000", hoje.plusDays(90), StatusProjeto.EM_ANDAMENTO, estrategiaA);
        cenarios.resultado(projeto1, gestor, hoje.minusMonths(2), "50000", 10.0, 4.0);
        cenarios.resultado(projeto1, gestor, hoje, "100000", 20.0, null);

        projeto2 = cenarios.projeto(gestor, "P2", "50000", hoje.minusDays(60), hoje.minusDays(1),
                StatusProjeto.EM_ANDAMENTO, estrategiaB);
        cenarios.resultado(projeto2, gestor, hoje, "20000", 5.0, 2.0);

        cenarios.projeto(gestor, "P3", "999999", hoje.plusDays(30), StatusProjeto.CANCELADO, estrategiaA);

        cenarios.ideiaAprovada(operador, gestor, "Ideia aprovada");
        cenarios.rejeitar(cenarios.ideia(operador, "Ideia rejeitada", null), gestor);
        cenarios.ideia(operador, "Ideia em análise", null);
    }

    @Test
    void resumoGeralConsideraProjetosNaoCancelados() throws Exception {
        get(lideranca, "/api/dashboard/resumo")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.investimentoTotal").value(150000.00))
                .andExpect(jsonPath("$.retornoTotal").value(170000.00))
                .andExpect(jsonPath("$.lucroTotal").value(20000.00))
                .andExpect(jsonPath("$.roiPercentual").value(13.33))
                .andExpect(jsonPath("$.ganhoProdutividadeMedio").value(10.0))
                .andExpect(jsonPath("$.reducaoCustosMedia").value(3.0))
                .andExpect(jsonPath("$.totalProjetos").value(3))
                .andExpect(jsonPath("$.projetosAtrasados").value(1));
    }

    @Test
    void resumoTrazContagensPorStatusEEtapaIncluindoZeros() throws Exception {
        get(lideranca, "/api/dashboard/resumo")
                .andExpect(jsonPath("$.projetosPorStatus.EM_ANDAMENTO").value(2))
                .andExpect(jsonPath("$.projetosPorStatus.CANCELADO").value(1))
                .andExpect(jsonPath("$.projetosPorStatus.PLANEJAMENTO").value(0))
                .andExpect(jsonPath("$.projetosPorStatus.CONCLUIDO").value(0))
                .andExpect(jsonPath("$.projetosPorEtapa.INICIACAO").value(3))
                .andExpect(jsonPath("$.projetosPorEtapa.EXECUCAO").value(0));
    }

    @Test
    void resumoTrazEstatisticasDeIdeiasEProximosPrazos() throws Exception {
        get(lideranca, "/api/dashboard/resumo")
                .andExpect(jsonPath("$.ideias.total").value(3))
                .andExpect(jsonPath("$.ideias.aprovadas").value(1))
                .andExpect(jsonPath("$.ideias.rejeitadas").value(1))
                .andExpect(jsonPath("$.ideias.emAnalise").value(1))
                .andExpect(jsonPath("$.ideias.taxaAprovacaoPercentual").value(50.0))
                .andExpect(jsonPath("$.proximosPrazos", hasSize(2)))
                .andExpect(jsonPath("$.proximosPrazos[0].nome").value("P2"))
                .andExpect(jsonPath("$.proximosPrazos[0].atrasado").value(true))
                .andExpect(jsonPath("$.proximosPrazos[0].diasParaPrazo").value(-1))
                .andExpect(jsonPath("$.proximosPrazos[1].nome").value("P1"));
    }

    @Test
    void resumoPodeSerFiltradoPorEstrategiaOuProjeto() throws Exception {
        get(lideranca, "/api/dashboard/resumo?estrategiaId=" + estrategiaA)
                .andExpect(jsonPath("$.investimentoTotal").value(100000.00))
                .andExpect(jsonPath("$.retornoTotal").value(150000.00))
                .andExpect(jsonPath("$.roiPercentual").value(50.0))
                .andExpect(jsonPath("$.totalProjetos").value(2))
                .andExpect(jsonPath("$.projetosAtrasados").value(0));

        get(lideranca, "/api/dashboard/resumo?projetoId=" + projeto2)
                .andExpect(jsonPath("$.investimentoTotal").value(50000.00))
                .andExpect(jsonPath("$.lucroTotal").value(-30000.00))
                .andExpect(jsonPath("$.roiPercentual").value(-60.0))
                .andExpect(jsonPath("$.projetosAtrasados").value(1));
    }

    @Test
    void semProjetosOsIndicadoresFicamZeradosSemErro() throws Exception {
        mongo.remove(new Query(), "projetos");

        get(lideranca, "/api/dashboard/resumo")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProjetos").value(0))
                .andExpect(jsonPath("$.investimentoTotal").value(0))
                .andExpect(jsonPath("$.roiPercentual").value(nullValue()))
                .andExpect(jsonPath("$.ganhoProdutividadeMedio").value(nullValue()));
    }

    @Test
    void retornoPorEstrategiaOrdenadoPeloLucroEComGrupoSemEstrategia() throws Exception {
        get(lideranca, "/api/dashboard/estrategias")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].titulo").value("Estratégia A"))
                .andExpect(jsonPath("$[0].projetos").value(1))
                .andExpect(jsonPath("$[0].investimento").value(100000.00))
                .andExpect(jsonPath("$[0].retorno").value(150000.00))
                .andExpect(jsonPath("$[0].lucro").value(50000.00))
                .andExpect(jsonPath("$[0].roiPercentual").value(50.0))
                .andExpect(jsonPath("$[1].titulo").value("Sem estratégia vinculada"))
                .andExpect(jsonPath("$[1].estrategiaId").value(nullValue()))
                .andExpect(jsonPath("$[1].ideias").value(3))
                .andExpect(jsonPath("$[2].titulo").value("Estratégia B"))
                .andExpect(jsonPath("$[2].lucro").value(-30000.00))
                .andExpect(jsonPath("$[2].roiPercentual").value(-60.0));
    }

    @Test
    void desempenhoDeCadaProjetoOrdenadoPeloLucro() throws Exception {
        get(lideranca, "/api/dashboard/projetos")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].nome").value("P1"))
                .andExpect(jsonPath("$[0].roiPercentual").value(50.0))
                .andExpect(jsonPath("$[0].ganhoProdutividade").value(15.0))
                .andExpect(jsonPath("$[0].atrasado").value(false))
                .andExpect(jsonPath("$[1].nome").value("P2"))
                .andExpect(jsonPath("$[1].atrasado").value(true))
                .andExpect(jsonPath("$[2].nome").value("P3"))
                .andExpect(jsonPath("$[2].status").value("CANCELADO"));
        get(lideranca, "/api/dashboard/projetos?estrategiaId=" + estrategiaA).andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void evolucaoMensalPreencheOsMesesSemResultadoComZero() throws Exception {
        String doisMesesAtras = YearMonth.from(hoje.minusMonths(2)).toString();
        String mesPassado = YearMonth.from(hoje.minusMonths(1)).toString();
        String esteMes = YearMonth.from(hoje).toString();

        get(lideranca, "/api/dashboard/evolucao")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].mes").value(doisMesesAtras))
                .andExpect(jsonPath("$[0].retornoNoMes").value(50000.00))
                .andExpect(jsonPath("$[0].retornoAcumulado").value(50000.00))
                .andExpect(jsonPath("$[1].mes").value(mesPassado))
                .andExpect(jsonPath("$[1].retornoNoMes").value(0.00))
                .andExpect(jsonPath("$[1].retornoAcumulado").value(50000.00))
                .andExpect(jsonPath("$[2].mes").value(esteMes))
                .andExpect(jsonPath("$[2].retornoNoMes").value(120000.00))
                .andExpect(jsonPath("$[2].retornoAcumulado").value(170000.00));

        get(lideranca, "/api/dashboard/evolucao?projetoId=" + projeto2)
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].retornoNoMes").value(20000.00));
        get(lideranca, "/api/dashboard/evolucao?projetoId=" + projeto1).andExpect(jsonPath("$", hasSize(3)));
    }
}
