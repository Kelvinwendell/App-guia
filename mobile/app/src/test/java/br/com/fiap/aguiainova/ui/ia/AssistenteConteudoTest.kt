package br.com.fiap.aguiainova.ui.ia

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import br.com.fiap.aguiainova.domain.model.MensagemChat
import br.com.fiap.aguiainova.domain.model.PapelMensagem
import br.com.fiap.aguiainova.domain.model.StatusIa
import br.com.fiap.aguiainova.ui.theme.AguiaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** SPEC-RD-009: remetente pela semantica (nao so cor), resposta por regras rotulada e status honesto. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AssistenteConteudoTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun remetenteEDitoParaOLeitorDeTela() {
        compose.setContent {
            AguiaTheme {
                Balao(MensagemChat(PapelMensagem.USUARIO, "Qual estratégia está em risco?"))
                Balao(MensagemChat(PapelMensagem.ASSISTENTE, "A de energia."))
            }
        }

        compose.onNodeWithContentDescription("Você: Qual estratégia está em risco?").assertExists()
        compose.onNodeWithContentDescription("Assistente: A de energia.").assertExists()
    }

    @Test
    fun respostaPorRegrasERotulada() {
        compose.setContent {
            AguiaTheme { Balao(MensagemChat(PapelMensagem.ASSISTENTE, "Foque em energia."), porRegras = true) }
        }

        compose.onNodeWithText("Leitura por regras (IA indisponível)", useUnmergedTree = true).assertExists()
    }

    @Test
    fun respostaLongaNaoECortada() {
        val longa = "Resposta detalhada. ".repeat(80).trim()
        compose.setContent { AguiaTheme { Balao(MensagemChat(PapelMensagem.ASSISTENTE, longa)) } }

        compose.onNodeWithText(longa, useUnmergedTree = true).assertExists()
    }

    @Test
    fun statusAindaNaoCarregadoNaoApareceComoConectado() {
        compose.setContent { AguiaTheme { SeloDeStatus(status = null, verificado = false) } }

        compose.onNodeWithText("Verificando a IA…").assertExists()
    }

    @Test
    fun statusQueFalhouDizQueNaoDeuParaVerificar() {
        compose.setContent { AguiaTheme { SeloDeStatus(status = null, verificado = true) } }

        compose.onNodeWithText("Não foi possível verificar a IA").assertExists()
    }

    @Test
    fun iaSemProvedorApareceComoNaoConfigurada() {
        compose.setContent {
            AguiaTheme { SeloDeStatus(StatusIa(habilitada = true, disponivel = false, provedor = "NENHUM", modelo = "-"), true) }
        }

        compose.onNodeWithText("IA não configurada no servidor").assertExists()
    }
}
