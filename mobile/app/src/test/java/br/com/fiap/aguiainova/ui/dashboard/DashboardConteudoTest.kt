package br.com.fiap.aguiainova.ui.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import br.com.fiap.aguiainova.domain.model.DesempenhoEstrategia
import br.com.fiap.aguiainova.domain.model.EvolucaoMensal
import br.com.fiap.aguiainova.domain.model.PrazoProjeto
import br.com.fiap.aguiainova.domain.model.ResumoDashboard
import br.com.fiap.aguiainova.domain.model.ResumoIdeias
import br.com.fiap.aguiainova.domain.model.StatusProjeto
import br.com.fiap.aguiainova.ui.theme.AguiaTheme
import br.com.fiap.aguiainova.util.Formatadores
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * SPEC-RD-008: metrica sem dado aparece como "—" (nao zero), prazos dizem atraso/data/progresso/status em texto,
 * lista vazia explica o porque, o comparativo avisa que e global e colunas sao lidas pelo leitor de tela.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DashboardConteudoTest {

    @get:Rule
    val compose = createComposeRule()

    private fun resumo(prazos: List<PrazoProjeto> = emptyList()) = ResumoDashboard(
        investimentoTotal = 0.0, retornoTotal = 0.0, lucroTotal = 0.0, roiPercentual = null,
        totalProjetos = 0, projetosAtrasados = 0, ideias = ResumoIdeias(0, 0, 0, 0), proximosPrazos = prazos
    )

    private fun dados(
        prazos: List<PrazoProjeto> = emptyList(),
        evolucao: List<EvolucaoMensal> = emptyList()
    ) = DadosDashboard(resumo(prazos), emptyList(), evolucao)

    @Test
    fun roiSemDadoApareceComoTracoENaoComoZero() {
        compose.setContent { AguiaTheme { Column { AbaResumo(dados()) } } }

        compose.onNodeWithText("Resultado financeiro").assertIsDisplayed()
        compose.onAllNodesWithText("—").assertCountEquals(4) // ROI, produtividade, reducao de custos, taxa de aprovacao
        compose.onAllNodesWithText("0,0%").assertCountEquals(0)
    }

    @Test
    fun semPrazosExplicaOMotivo() {
        compose.setContent { AguiaTheme { Column { AbaResumo(dados()) } } }

        compose.onNodeWithText("Nenhum projeto em andamento com prazo definido", substring = true).assertExists()
    }

    @Test
    fun prazoAtrasadoDizAtrasoDataProgressoEStatusEmTexto() {
        val prazo = PrazoProjeto("p1", "Sensores IoT", StatusProjeto.EM_ANDAMENTO, 40, "2026-01-10", -5, atrasado = true)
        compose.setContent { AguiaTheme { Column { AbaResumo(dados(prazos = listOf(prazo))) } } }

        compose.onNodeWithText("Atrasado desde 10/01/2026 · 40% concluído · ${StatusProjeto.EM_ANDAMENTO.rotulo}")
            .assertExists()
    }

    @Test
    fun comparativoPorEstrategiaAvisaQueEGlobal() {
        val porEstrategia = listOf(
            DesempenhoEstrategia("e1", "Energia", projetos = 1, ideias = 1, investimento = 1.0, retorno = 0.5, lucro = -0.5, roiPercentual = -50.0)
        )
        compose.setContent {
            AguiaTheme { Column { AbaEstrategias(DadosDashboard(resumo(), porEstrategia, emptyList())) {} } }
        }

        compose.onNodeWithText("ROI por estratégia (todas)").assertExists()
        compose.onNodeWithText("-50,0%").assertExists()
    }

    @Test
    fun colunasDeRetornoSaoLidasComValor() {
        val evolucao = listOf(EvolucaoMensal("2026-01", 0.0, 0.0), EvolucaoMensal("2026-02", -500.0, -500.0))
        compose.setContent { AguiaTheme { Column { AbaProjetos(dados(evolucao = evolucao)) } } }

        // Zero e negativo continuam legiveis (com sinal) para o leitor de tela, ja que a coluna nao mostra o numero.
        val janeiro = "${Formatadores.mes("2026-01")}: ${Formatadores.moedaCompacta(0.0)}"
        val fevereiro = "${Formatadores.mes("2026-02")}: ${Formatadores.moedaCompacta(-500.0)}"
        compose.onNodeWithContentDescription(janeiro).assertExists()
        compose.onNodeWithContentDescription(fevereiro).assertExists()
        assertTrue(fevereiro.contains("-"))
    }
}
