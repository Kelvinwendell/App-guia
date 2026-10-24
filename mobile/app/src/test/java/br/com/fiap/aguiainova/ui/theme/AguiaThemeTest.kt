package br.com.fiap.aguiainova.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * O app era forcado dark-only (Theme.kt so tinha darkColorScheme). Estes testes garantem que
 * AguiaTheme(escuroForcado) realmente alterna entre os dois esquemas, que cores de marca/status
 * continuam identicas nos dois temas (reconhecimento visual), e que tons neutros (fundo, texto
 * secundario) mudam de verdade entre claro e escuro.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AguiaThemeTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun temaClaroUsaEsquemaClaroDeFundo() {
        var fundo: Color? = null
        compose.setContent {
            AguiaTheme(escuroForcado = false) { fundo = MaterialTheme.colorScheme.background }
        }
        compose.waitForIdle()

        assertEquals(NeutrosClaro.Fundo, fundo)
    }

    @Test
    fun temaEscuroUsaEsquemaEscuroDeFundo() {
        var fundo: Color? = null
        compose.setContent {
            AguiaTheme(escuroForcado = true) { fundo = MaterialTheme.colorScheme.background }
        }
        compose.waitForIdle()

        assertEquals(NeutrosEscuro.Fundo, fundo)
    }

    @Test
    fun corPrimariaEIgualNosDoisTemas() {
        var primariaClara: Color? = null
        var primariaEscura: Color? = null
        compose.setContent {
            Column {
                AguiaTheme(escuroForcado = false) { primariaClara = MaterialTheme.colorScheme.primary }
                AguiaTheme(escuroForcado = true) { primariaEscura = MaterialTheme.colorScheme.primary }
            }
        }
        compose.waitForIdle()

        assertEquals(AguiaCores.Primaria, primariaClara)
        assertEquals(AguiaCores.Primaria, primariaEscura)
    }

    @Test
    fun corDeErroEIgualNosDoisTemas() {
        var erroClaro: Color? = null
        var erroEscuro: Color? = null
        compose.setContent {
            Column {
                AguiaTheme(escuroForcado = false) { erroClaro = MaterialTheme.colorScheme.error }
                AguiaTheme(escuroForcado = true) { erroEscuro = MaterialTheme.colorScheme.error }
            }
        }
        compose.waitForIdle()

        assertEquals(AguiaCores.Erro, erroClaro)
        assertEquals(AguiaCores.Erro, erroEscuro)
    }

    @Test
    fun textoSecundarioMudaEntreTemas() {
        var textoClaro: Color? = null
        var textoEscuro: Color? = null
        compose.setContent {
            Column {
                AguiaTheme(escuroForcado = false) { textoClaro = MaterialTheme.colorScheme.onSurfaceVariant }
                AguiaTheme(escuroForcado = true) { textoEscuro = MaterialTheme.colorScheme.onSurfaceVariant }
            }
        }
        compose.waitForIdle()

        assertEquals(NeutrosClaro.TextoSecundario, textoClaro)
        assertEquals(NeutrosEscuro.TextoSecundario, textoEscuro)
        assertNotEquals(textoClaro, textoEscuro)
    }

    @Test
    fun bordaMudaEntreTemas() {
        var bordaClara: Color? = null
        var bordaEscura: Color? = null
        compose.setContent {
            Column {
                AguiaTheme(escuroForcado = false) { bordaClara = MaterialTheme.colorScheme.outline }
                AguiaTheme(escuroForcado = true) { bordaEscura = MaterialTheme.colorScheme.outline }
            }
        }
        compose.waitForIdle()

        assertEquals(NeutrosClaro.Borda, bordaClara)
        assertEquals(NeutrosEscuro.Borda, bordaEscura)
        assertNotEquals(bordaClara, bordaEscura)
    }
}
