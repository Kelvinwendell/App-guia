package br.com.fiap.aguiainova.ui.auth

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import br.com.fiap.aguiainova.ui.theme.AguiaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * SPEC-RD-003: o "Concluir" do teclado no campo de senha entra (antes so fechava o teclado), o botao nao reenvia
 * enquanto carrega e a troca de servidor so aparece quando o build permite.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AutenticacaoConteudoTest {

    @get:Rule
    val compose = createComposeRule()

    private fun conteudo(
        ocupado: Boolean = false,
        servidor: String? = null,
        aoEntrar: () -> Unit = {}
    ) = compose.setContent {
        AguiaTheme {
            LoginConteudo(
                email = "gestor@empresa.com", aoMudarEmail = {}, senha = "123456", aoMudarSenha = {},
                ocupado = ocupado, aoEntrar = aoEntrar, aoCriarConta = {}, servidor = servidor, aoEditarServidor = {}
            )
        }
    }

    @Test
    fun concluirNoTecladoDaSenhaEntra() {
        var entradas = 0
        conteudo(aoEntrar = { entradas++ })

        compose.onNode(hasSetTextAction() and hasText("Senha")).performImeAction()

        assertEquals(1, entradas)
    }

    @Test
    fun botaoEntrarChamaOCallbackUmaVez() {
        var entradas = 0
        conteudo(aoEntrar = { entradas++ })

        compose.onNodeWithText("Entrar").performClick()

        assertEquals(1, entradas)
    }

    @Test
    fun botaoNaoReenviaEnquantoCarrega() {
        var entradas = 0
        conteudo(ocupado = true, aoEntrar = { entradas++ })

        compose.onAllNodesWithText("Entrar").assertCountEquals(0) // o botao mostra o indicador no lugar do texto
        assertEquals(0, entradas)
    }

    @Test
    fun trocaDeServidorSoApareceQuandoPermitida() {
        conteudo(servidor = null)

        compose.onAllNodesWithText("Servidor:", substring = true).assertCountEquals(0)
    }

    @Test
    fun trocaDeServidorMostraOEnderecoAtual() {
        conteudo(servidor = "http://10.0.2.2:8080/")

        compose.onNodeWithText("Servidor: http://10.0.2.2:8080/").assertExists()
    }
}
