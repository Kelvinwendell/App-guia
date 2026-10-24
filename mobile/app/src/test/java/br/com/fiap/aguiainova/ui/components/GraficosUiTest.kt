package br.com.fiap.aguiainova.ui.components

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import br.com.fiap.aguiainova.ui.theme.AguiaTheme
import br.com.fiap.aguiainova.util.Formatadores
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** SPEC-RD-008: [0, 100, -50] mantem rotulos e sinais visiveis; 24 meses rolam em vez de espremer as colunas. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GraficosUiTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun barrasMostramZeroPositivoENegativoComSinal() {
        compose.setContent {
            AguiaTheme {
                GraficoBarras(
                    listOf(ItemGrafico("Zero", 0.0), ItemGrafico("Ganho", 100.0), ItemGrafico("Perda", -50.0)),
                    Formatadores::percentual
                )
            }
        }

        listOf("Zero", "Ganho", "Perda").forEach { compose.onAllNodesWithText(it).assertCountEquals(1) }
        compose.onNodeWithText(Formatadores.percentual(0.0)).assertExists()
        compose.onNodeWithText(Formatadores.percentual(100.0)).assertExists()
        compose.onNodeWithText(Formatadores.percentual(-50.0)).assertExists()
    }

    @Test
    fun vinteEQuatroMesesNaoEspremeAsColunas() {
        val meses = (1..24).map { ItemGrafico("m$it", it * 10.0) }
        compose.setContent {
            AguiaTheme { GraficoColunas(meses, Modifier.width(320.dp), formatarValor = { it.toInt().toString() }) }
        }

        // Cada coluna continua acessivel, inclusive as que ficaram fora da area visivel (rolagem dentro do grafico).
        compose.onNodeWithContentDescription("m1: 10").assertExists()
        compose.onNodeWithContentDescription("m24: 240").assertExists()
        val primeira = compose.onNodeWithContentDescription("m1: 10").getUnclippedBoundsInRoot()
        assertTrue(primeira.right - primeira.left >= 40.dp)
    }
}
