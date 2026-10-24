package br.com.fiap.aguiainova.ui.ideias

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import br.com.fiap.aguiainova.domain.model.AvaliacaoIa
import br.com.fiap.aguiainova.domain.model.Ideia
import br.com.fiap.aguiainova.domain.model.Perfil
import br.com.fiap.aguiainova.domain.model.Prioridade
import br.com.fiap.aguiainova.domain.model.StatusIdeia
import br.com.fiap.aguiainova.ui.theme.AguiaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * SPEC-RD-005: ideia EM_ANALISE propria mostra Editar/Excluir; aprovada de outro autor nao; gestor
 * sempre ve Aprovar/Rejeitar e nunca Editar/Excluir.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CartaoIdeiaTest {

    @get:Rule
    val compose = createComposeRule()

    private fun ideia(status: StatusIdeia = StatusIdeia.EM_ANALISE, avaliacaoIa: AvaliacaoIa? = null) = Ideia(
        id = "1", titulo = "Ideia", descricao = "Descrição", categoria = "Tecnologia",
        status = status, prioridade = Prioridade.MEDIA, autorId = "a", autorNome = "Vinícius", avaliacaoIa = avaliacaoIa
    )

    @Test
    fun ideiaPropriaEmAnaliseMostraEditarEExcluir() {
        compose.setContent {
            AguiaTheme {
                CartaoIdeia(
                    ideia = ideia(status = StatusIdeia.EM_ANALISE), perfil = Perfil.OPERADOR,
                    mostrarAcoesDoAutor = true, ocupado = false,
                    aoEditar = {}, aoExcluir = {}, aoDecidir = {}, aoPontuar = {}
                )
            }
        }

        compose.onNodeWithText("Editar").assertIsDisplayed()
        compose.onNodeWithText("Excluir").assertIsDisplayed()
    }

    @Test
    fun ideiaAprovadaDeOutroAutorNaoMostraEditarNemExcluir() {
        compose.setContent {
            AguiaTheme {
                CartaoIdeia(
                    ideia = ideia(status = StatusIdeia.APROVADA), perfil = Perfil.OPERADOR,
                    mostrarAcoesDoAutor = false, ocupado = false,
                    aoEditar = {}, aoExcluir = {}, aoDecidir = {}, aoPontuar = {}
                )
            }
        }

        compose.onAllNodesWithText("Editar").assertCountEquals(0)
        compose.onAllNodesWithText("Excluir").assertCountEquals(0)
    }

    @Test
    fun gestorVeAprovarERejeitarENuncaEditarOuExcluir() {
        compose.setContent {
            AguiaTheme {
                CartaoIdeia(
                    ideia = ideia(), perfil = Perfil.GESTOR,
                    mostrarAcoesDoAutor = false, ocupado = false,
                    aoEditar = {}, aoExcluir = {}, aoDecidir = {}, aoPontuar = {}
                )
            }
        }

        compose.onNodeWithText("Aprovar").assertIsDisplayed()
        compose.onNodeWithText("Rejeitar").assertIsDisplayed()
        compose.onAllNodesWithText("Editar").assertCountEquals(0)
        compose.onAllNodesWithText("Excluir").assertCountEquals(0)
    }

    @Test
    fun tocarAprovarChamaDecidirComStatusAprovada() {
        var statusRecebido: StatusIdeia? = null
        compose.setContent {
            AguiaTheme {
                CartaoIdeia(
                    ideia = ideia(), perfil = Perfil.GESTOR,
                    mostrarAcoesDoAutor = false, ocupado = false,
                    aoEditar = {}, aoExcluir = {}, aoDecidir = { statusRecebido = it }, aoPontuar = {}
                )
            }
        }

        compose.onNodeWithText("Aprovar").performClick()

        assertEquals(StatusIdeia.APROVADA, statusRecebido)
    }

    @Test
    fun ideiaSemNotaNaoMostraAvaliacao() {
        compose.setContent {
            AguiaTheme {
                CartaoIdeia(
                    ideia = ideia(avaliacaoIa = null), perfil = Perfil.GESTOR,
                    mostrarAcoesDoAutor = false, ocupado = false,
                    aoEditar = {}, aoExcluir = {}, aoDecidir = {}, aoPontuar = {}
                )
            }
        }

        compose.onAllNodesWithText("Nota", substring = true).assertCountEquals(0)
    }
}
