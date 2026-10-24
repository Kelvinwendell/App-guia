package br.com.fiap.aguiainova.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import br.com.fiap.aguiainova.ui.theme.AguiaCores
import br.com.fiap.aguiainova.ui.theme.AguiaTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** SPEC-RD-007: selos quebram de linha em vez de vazar, e filtros empilham em tela estreita ou fonte ampliada. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ResponsivoTest {

    @get:Rule
    val compose = createComposeRule()

    // Robolectric mede texto com largura irreal; caixas de tamanho fixo tornam o teste deterministico.
    @Test
    fun itensQueNaoCabemVaoParaALinhaDeBaixo() {
        compose.setContent {
            LinhaQuebravel(Modifier.width(200.dp)) {
                Box(Modifier.size(80.dp, 20.dp).testTag("a"))
                Box(Modifier.size(150.dp, 20.dp).testTag("b"))
            }
        }

        val a = compose.onNodeWithTag("a").getUnclippedBoundsInRoot()
        val b = compose.onNodeWithTag("b").getUnclippedBoundsInRoot()
        assertTrue(b.top >= a.bottom)
        assertEquals(0.dp, b.left)
    }

    @Test
    fun itensQueCabemFicamNaMesmaLinha() {
        compose.setContent {
            LinhaQuebravel(Modifier.width(200.dp)) {
                Box(Modifier.size(80.dp, 20.dp).testTag("a"))
                Box(Modifier.size(80.dp, 20.dp).testTag("b"))
            }
        }

        val a = compose.onNodeWithTag("a").getUnclippedBoundsInRoot()
        val b = compose.onNodeWithTag("b").getUnclippedBoundsInRoot()
        assertEquals(a.top, b.top)
        assertEquals(88.dp, b.left)
    }

    @Test
    fun seloLongoContinuaVisivel() {
        compose.setContent {
            AguiaTheme {
                LinhaQuebravel(Modifier.width(200.dp)) {
                    Selo("Inovação", AguiaCores.Primaria)
                    Selo("Campanha de verão muito longa", AguiaCores.Alerta)
                }
            }
        }

        compose.onNodeWithText("Campanha de verão muito longa").assertIsDisplayed()
    }

    @Test
    fun filtrosFicamLadoALadoComEspaco() {
        compose.setContent {
            AguiaTheme {
                LadoALadoOuEmpilhado(Modifier.width(400.dp)) { largura ->
                    Text("Categoria", largura)
                    Text("Campanha", largura)
                }
            }
        }

        val categoria = compose.onNodeWithText("Categoria").getUnclippedBoundsInRoot()
        val campanha = compose.onNodeWithText("Campanha").getUnclippedBoundsInRoot()
        assertEquals(categoria.top, campanha.top)
    }

    @Test
    fun filtrosEmpilhamComFonteAmpliada() {
        compose.setContent {
            val densidade = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(densidade.density, fontScale = 1.5f)) {
                AguiaTheme {
                    LadoALadoOuEmpilhado(Modifier.width(360.dp)) { largura ->
                        Text("Categoria", largura)
                        Text("Campanha", largura)
                    }
                }
            }
        }

        val categoria = compose.onNodeWithText("Categoria").getUnclippedBoundsInRoot()
        val campanha = compose.onNodeWithText("Campanha").getUnclippedBoundsInRoot()
        assertTrue(campanha.top >= categoria.bottom)
    }
}
