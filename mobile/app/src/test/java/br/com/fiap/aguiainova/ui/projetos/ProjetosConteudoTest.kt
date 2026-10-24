package br.com.fiap.aguiainova.ui.projetos

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import br.com.fiap.aguiainova.domain.model.EtapaProjeto
import br.com.fiap.aguiainova.domain.model.Projeto
import br.com.fiap.aguiainova.domain.model.ResultadoProjeto
import br.com.fiap.aguiainova.domain.model.StatusProjeto
import br.com.fiap.aguiainova.ui.theme.AguiaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * SPEC-RD-006: lideranca consulta o detalhe sem nenhuma acao de escrita; o gestor tem todas; com muitos resultados e
 * nome extenso a ultima acao ("Excluir projeto") continua alcancavel.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ProjetosConteudoTest {

    @get:Rule
    val compose = createComposeRule()

    private fun projeto(resultados: Int = 1, nome: String = "Sensores IoT na linha 3") = Projeto(
        id = "p1", nome = nome, descricao = "Monitorar consumo por máquina.", status = StatusProjeto.EM_ANDAMENTO,
        etapa = EtapaProjeto.entries.first(), investimento = 50_000.0, prazo = "2026-12-31", progresso = 40,
        resultados = (1..resultados).map { ResultadoProjeto("r$it", "2026-0${(it % 9) + 1}-10", "Resultado $it", 1_000.0 * it) }
    )

    private fun conteudo(
        gestor: Boolean,
        projeto: Projeto = projeto(),
        aoExcluir: () -> Unit = {},
        aoRemover: (ResultadoProjeto) -> Unit = {}
    ) = compose.setContent {
        AguiaTheme {
            ProjetoDetalheConteudo(
                projeto = projeto, gestor = gestor, ocupado = false, aoSalvarAndamento = {},
                aoRemoverResultado = aoRemover, aoAdicionarResultado = {}, aoExcluir = aoExcluir
            )
        }
    }

    @Test
    fun liderancaConsultaSemNenhumaAcaoDeEscrita() {
        conteudo(gestor = false)

        compose.onNodeWithText("Resultado 1").assertExists()
        listOf("Atualizar andamento", "Salvar andamento", "Adicionar resultado", "Excluir projeto").forEach {
            compose.onAllNodesWithText(it).assertCountEquals(0)
        }
        compose.onAllNodesWithContentDescription("Remover resultado").assertCountEquals(0)
    }

    @Test
    fun gestorTemAsAcoesDeEscrita() {
        conteudo(gestor = true)

        compose.onNodeWithText("Atualizar andamento").assertExists()
        compose.onNodeWithText("Adicionar resultado").assertExists()
        compose.onAllNodesWithContentDescription("Remover resultado").assertCountEquals(1)
    }

    @Test
    fun removerResultadoIdentificaQualResultado() {
        var removido: ResultadoProjeto? = null
        conteudo(gestor = true, projeto = projeto(resultados = 2), aoRemover = { removido = it })

        compose.onAllNodesWithContentDescription("Remover resultado")[1].performScrollTo().performClick()

        assertEquals("r2", removido?.id)
    }

    @Test
    fun muitosResultadosENomeExtensoNaoPerdemAUltimaAcao() {
        var exclusoes = 0
        val nomeLongo = "Projeto de modernização ".repeat(5).trim() // 120 caracteres
        conteudo(gestor = true, projeto = projeto(resultados = 15, nome = nomeLongo), aoExcluir = { exclusoes++ })

        compose.onNodeWithText("Excluir projeto").performScrollTo().performClick()

        assertEquals(1, exclusoes)
    }
}
