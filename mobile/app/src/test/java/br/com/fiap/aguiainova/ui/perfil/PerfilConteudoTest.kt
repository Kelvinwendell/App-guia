package br.com.fiap.aguiainova.ui.perfil

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import br.com.fiap.aguiainova.domain.model.Perfil
import br.com.fiap.aguiainova.domain.model.Usuario
import br.com.fiap.aguiainova.ui.theme.AguiaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * SPEC-RD-003: ausencia de cargo/departamento/unidade nao produz "null" na tela; Sair chama o callback.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PerfilConteudoTest {

    @get:Rule
    val compose = createComposeRule()

    private fun usuario(cargo: String? = null, departamento: String? = null, unidade: String? = null) = Usuario(
        id = "1",
        nome = "Vinícius",
        email = "vinicius@gmail.com",
        perfil = Perfil.OPERADOR,
        cargo = cargo,
        departamento = departamento,
        unidade = unidade
    )

    @Test
    fun camposProfissionaisAusentesNaoAparecemComoNull() {
        compose.setContent {
            AguiaTheme { PerfilConteudo(usuario(), Perfil.OPERADOR, aoSair = {}) }
        }

        compose.onNodeWithText("Vinícius").assertIsDisplayed()
        compose.onAllNodesWithText("null", substring = true).assertCountEquals(0)
    }

    @Test
    fun campoProfissionalPresenteAparece() {
        compose.setContent {
            AguiaTheme { PerfilConteudo(usuario(cargo = "Engenheira"), Perfil.OPERADOR, aoSair = {}) }
        }

        compose.onNodeWithText("Engenheira").assertIsDisplayed()
    }

    @Test
    fun semUsuarioNaoMostraNomeOuEmailComoNull() {
        compose.setContent {
            AguiaTheme { PerfilConteudo(usuario = null, perfil = Perfil.OPERADOR, aoSair = {}) }
        }

        compose.onAllNodesWithText("null", substring = true).assertCountEquals(0)
    }

    @Test
    fun tocarSairChamaOCallback() {
        var chamadas = 0
        compose.setContent {
            AguiaTheme { PerfilConteudo(usuario(), Perfil.OPERADOR, aoSair = { chamadas++ }) }
        }

        compose.onNodeWithText("Sair da conta").performClick()

        assertEquals(1, chamadas)
    }
}
