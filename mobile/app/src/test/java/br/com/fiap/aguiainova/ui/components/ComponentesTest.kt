package br.com.fiap.aguiainova.ui.components

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import br.com.fiap.aguiainova.ui.theme.AguiaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private fun hasErrorSemantica() = SemanticsMatcher("tem semantica de erro") { it.config.getOrNull(SemanticsProperties.Error) != null }

/**
 * SPEC-RD-002: campo com erro expoe semantica de erro e mantem o valor digitado; botao
 * carregando fica desabilitado (nao reenviar pela UI enquanto a chamada esta em andamento).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ComponentesTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun campoComErroExpoeSemanticaDeErroEMantemValor() {
        compose.setContent {
            AguiaTheme {
                CampoTexto(valor = "texto digitado", aoMudar = {}, rotulo = "Nome", erro = "Campo obrigatório")
            }
        }

        compose.onNodeWithText("texto digitado").assertIsDisplayed()
        compose.onNodeWithText("Campo obrigatório").assertIsDisplayed()
        compose.onNode(hasErrorSemantica()).assertIsDisplayed()
    }

    @Test
    fun campoSemErroNaoExpoeSemanticaDeErro() {
        compose.setContent {
            AguiaTheme {
                CampoTexto(valor = "", aoMudar = {}, rotulo = "Nome")
            }
        }

        compose.onAllNodes(hasErrorSemantica()).assertCountEquals(0)
    }

    @Test
    fun campoComErroSubstituiDica() {
        compose.setContent {
            AguiaTheme {
                CampoTexto(valor = "", aoMudar = {}, rotulo = "Nome", dica = "Dica normal", erro = "Erro prioritario")
            }
        }

        compose.onNodeWithText("Erro prioritario").assertIsDisplayed()
    }

    @Test
    fun botaoCarregandoFicaDesabilitado() {
        compose.setContent {
            AguiaTheme {
                BotaoPrimario(texto = "Enviar", aoClicar = {}, carregando = true)
            }
        }

        compose.onNode(hasClickAction()).assertIsNotEnabled()
    }

    @Test
    fun botaoNaoCarregandoFicaHabilitado() {
        compose.setContent {
            AguiaTheme {
                BotaoPrimario(texto = "Enviar", aoClicar = {}, carregando = false)
            }
        }

        compose.onNodeWithText("Enviar").assertIsDisplayed()
    }
}
