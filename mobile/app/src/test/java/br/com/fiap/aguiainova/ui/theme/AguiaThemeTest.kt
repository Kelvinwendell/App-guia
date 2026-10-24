package br.com.fiap.aguiainova.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * O app e dark-only por decisao de produto (docs/redesign/PRD.md, "Nao objetivos": nao criar tema
 * claro). Estes testes garantem que AguiaTheme expoe os tokens de AguiaCores via MaterialTheme.colorScheme,
 * como pede docs/redesign/ARQUITETURA.md ("Usar MaterialTheme.colorScheme... tokens compartilhados").
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AguiaThemeTest {

    @get:Rule
    val compose = createComposeRule()

    private fun lerDoTema(leitor: @Composable () -> Color): Color {
        var cor: Color? = null
        compose.setContent { AguiaTheme { cor = leitor() } }
        compose.waitForIdle()
        return cor!!
    }

    @Test
    fun fundoUsaAguiaCoresFundo() {
        val fundo = lerDoTema { MaterialTheme.colorScheme.background }
        assertEquals(AguiaCores.Fundo, fundo)
    }

    @Test
    fun corPrimariaUsaAguiaCoresPrimaria() {
        val primaria = lerDoTema { MaterialTheme.colorScheme.primary }
        assertEquals(AguiaCores.Primaria, primaria)
    }

    @Test
    fun corDeErroUsaAguiaCoresErro() {
        val erro = lerDoTema { MaterialTheme.colorScheme.error }
        assertEquals(AguiaCores.Erro, erro)
    }

    @Test
    fun textoSecundarioUsaAguiaCoresTextoSecundario() {
        val texto = lerDoTema { MaterialTheme.colorScheme.onSurfaceVariant }
        assertEquals(AguiaCores.TextoSecundario, texto)
    }

    @Test
    fun bordaUsaAguiaCoresBorda() {
        val borda = lerDoTema { MaterialTheme.colorScheme.outline }
        assertEquals(AguiaCores.Borda, borda)
    }
}
