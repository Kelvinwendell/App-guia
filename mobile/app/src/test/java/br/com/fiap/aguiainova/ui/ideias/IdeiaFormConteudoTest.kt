package br.com.fiap.aguiainova.ui.ideias

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import br.com.fiap.aguiainova.domain.model.DadosIdeia
import br.com.fiap.aguiainova.domain.model.Ideia
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
 * SPEC-RD-005: erros de campo so depois da primeira tentativa de salvar, sem perder o que foi digitado; edicao abre
 * preenchida; o botao nao reenvia enquanto salva.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class IdeiaFormConteudoTest {

    @get:Rule
    val compose = createComposeRule()

    private fun formulario(
        existente: Ideia? = null,
        ocupado: Boolean = false,
        aoSalvar: (DadosIdeia) -> Unit = {}
    ) = compose.setContent {
        AguiaTheme { IdeiaFormConteudo(FormularioIdeia(existente, emptyList()), ocupado, aoSalvar) }
    }

    @Test
    fun errosSoAparecemDepoisDeTentarSalvar() {
        formulario()

        compose.onAllNodesWithText("Informe um título.").assertCountEquals(0)
        compose.onNodeWithText("Salvar ideia").performScrollTo().performClick()

        compose.onNodeWithText("Informe um título.").assertExists()
        compose.onNodeWithText("Descreva a ideia.").assertExists()
        compose.onNodeWithText("Informe uma categoria.").assertExists()
    }

    @Test
    fun textoDigitadoContinuaDepoisDoErro() {
        var enviado: DadosIdeia? = null
        formulario(aoSalvar = { enviado = it })

        compose.onNode(hasSetTextAction() and hasText("Título")).performTextInput("Sensor de vazamento")
        compose.onNodeWithText("Salvar ideia").performScrollTo().performClick()

        compose.onNodeWithText("Sensor de vazamento").assertExists()
        compose.onAllNodesWithText("Informe um título.").assertCountEquals(0)
        compose.onNodeWithText("Descreva a ideia.").assertExists()
        assertEquals("Sensor de vazamento", enviado?.titulo)
    }

    @Test
    fun edicaoAbrePreenchida() {
        val ideia = Ideia(
            "i1", "Reuso de água", "Captar água da chuva.", "Sustentabilidade", StatusIdeia.EM_ANALISE,
            Prioridade.entries.first(), "u1", "Ana"
        )
        formulario(existente = ideia)

        compose.onNodeWithText("Reuso de água").assertExists()
        compose.onNodeWithText("Captar água da chuva.").assertExists()
        compose.onNodeWithText("Sustentabilidade").assertExists()
    }

    @Test
    fun naoReenviaEnquantoSalva() {
        var envios = 0
        formulario(ocupado = true, aoSalvar = { envios++ })

        compose.onAllNodesWithText("Salvar ideia").assertCountEquals(0) // indicador no lugar do texto, botao desabilitado
        assertEquals(0, envios)
    }
}
