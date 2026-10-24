package br.com.fiap.aguiainova.ui.components

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import br.com.fiap.aguiainova.ui.common.Estado
import br.com.fiap.aguiainova.ui.theme.AguiaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * SPEC-RD-002: erro inicial mostra mensagem e permite tentar de novo; vazio sem acao completa
 * nao cria botao algum (acaoTexto e aoAcao so valem juntos).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class EstadosTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun erroMostraMensagemEBotaoDeTentarNovamente() {
        compose.setContent {
            AguiaTheme {
                ConteudoDoEstado(estado = Estado.Erro("Falha ao carregar"), aoTentarNovamente = {}) {}
            }
        }

        compose.onNodeWithText("Falha ao carregar").assertIsDisplayed()
        compose.onNodeWithText("Tentar novamente").assertIsDisplayed()
    }

    @Test
    fun tocarTentarNovamenteChamaCallbackUmaUnicaVez() {
        var chamadas = 0
        compose.setContent {
            AguiaTheme {
                ConteudoDoEstado(estado = Estado.Erro("Falha"), aoTentarNovamente = { chamadas++ }) {}
            }
        }

        compose.onNodeWithText("Tentar novamente").performClick()

        assertEquals(1, chamadas)
    }

    @Test
    fun vazioSemAcaoNaoCriaBotao() {
        compose.setContent {
            AguiaTheme { MensagemVazia("Nada por aqui ainda.") }
        }

        compose.onNodeWithText("Nada por aqui ainda.").assertIsDisplayed()
        compose.onAllNodesWithText("Criar", substring = true).assertCountEquals(0)
    }

    @Test
    fun vazioComAcaoTextoMasSemCallbackNaoCriaBotao() {
        compose.setContent {
            AguiaTheme { MensagemVazia("Nada por aqui ainda.", acaoTexto = "Criar a primeira ideia") }
        }

        compose.onAllNodesWithText("Criar a primeira ideia").assertCountEquals(0)
    }

    @Test
    fun vazioComAcaoCompletaCriaBotaoQueChamaCallback() {
        var chamadas = 0
        compose.setContent {
            AguiaTheme {
                MensagemVazia("Nada por aqui ainda.", acaoTexto = "Criar a primeira ideia", aoAcao = { chamadas++ })
            }
        }

        compose.onNodeWithText("Criar a primeira ideia").performClick()

        assertEquals(1, chamadas)
    }
}
