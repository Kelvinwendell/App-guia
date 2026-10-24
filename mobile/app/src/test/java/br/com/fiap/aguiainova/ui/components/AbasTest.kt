package br.com.fiap.aguiainova.ui.components

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import br.com.fiap.aguiainova.ui.theme.AguiaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** SPEC-RD-002: com mais abas do que cabem, todas continuam alcancaveis (rolagem) e selecionaveis. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AbasTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun abaForaDaTelaPodeSerAlcancadaESelecionada() {
        var escolhida = 0
        compose.setContent {
            AguiaTheme {
                AbasDaTela(listOf("Resumo", "Estratégias", "Projetos", "Insights IA"), 0, { escolhida = it }, Modifier.width(240.dp))
            }
        }

        compose.onNodeWithText("Insights IA").performScrollTo().performClick()

        assertEquals(3, escolhida)
    }

    @Test
    fun ateTresAbasDividemALargura() {
        var escolhida = -1
        compose.setContent {
            AguiaTheme { AbasDaTela(listOf("Vigentes", "Encerradas", "Histórico"), 0, { escolhida = it }) }
        }

        compose.onNodeWithText("Histórico").performClick()

        assertEquals(2, escolhida)
    }
}
