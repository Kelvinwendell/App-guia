package br.com.fiap.aguiainova.ui.dashboard

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import br.com.fiap.aguiainova.domain.model.InsightsIa
import br.com.fiap.aguiainova.ui.common.Estado
import br.com.fiap.aguiainova.ui.theme.AguiaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** SPEC-RD-009: leitura por regras rotulada, leitura global explicita e sem repetir o pedido enquanto carrega. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class InsightsConteudoTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun insightsPorRegrasSaoRotulados() {
        compose.setContent {
            AguiaTheme { BlocoInsights(Estado.Pronto(InsightsIa("Resumo", fonte = "HEURISTICA")), aoGerar = {}) }
        }

        compose.onNodeWithText("Leitura por regras (IA indisponível)").assertExists()
    }

    @Test
    fun deixaClaroQueALeituraEGeral() {
        compose.setContent { AguiaTheme { BlocoInsights(null, aoGerar = {}) } }

        compose.onNodeWithText("Leitura geral (todas as estratégias)", substring = true).assertExists()
    }

    @Test
    fun botaoFicaDesabilitadoEnquantoCarrega() {
        compose.setContent { AguiaTheme { BlocoInsights(Estado.Carregando, aoGerar = {}) } }

        compose.onNodeWithText("Gerar novamente", substring = true).assertIsNotEnabled()
    }
}
