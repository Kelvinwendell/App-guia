package br.com.fiap.aguiainova.navigation

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import br.com.fiap.aguiainova.domain.model.Perfil
import br.com.fiap.aguiainova.ui.components.BarraInferior
import br.com.fiap.aguiainova.ui.theme.AguiaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * SPEC-RD-002: as abas da barra inferior correspondem exatamente ao perfil - nenhuma aba de gestao
 * aparece para quem nao tem a permissao (ex.: Operador nao ve Projetos nem Painel). A tela de estrategias aparece
 * como "Estrategia" (operador e lideranca) ou "Direcao" (gestor).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NavegacaoPorPerfilTest {

    @get:Rule
    val compose = createComposeRule()

    private fun renderizarBarra(perfil: Perfil) {
        compose.setContent {
            AguiaTheme {
                val nav = rememberNavController()
                BarraInferior(perfil, nav)
            }
        }
    }

    @Test
    fun operadorVeInicioIdeiasEstrategiaPerfilENadaMais() {
        renderizarBarra(Perfil.OPERADOR)

        compose.onNodeWithText("Início").assertIsDisplayed()
        compose.onNodeWithText("Ideias").assertIsDisplayed()
        compose.onNodeWithText("Estratégia").assertIsDisplayed()
        compose.onNodeWithText("Perfil").assertIsDisplayed()
        compose.onAllNodesWithText("Projetos").assertCountEquals(0)
        compose.onAllNodesWithText("Painel").assertCountEquals(0)
        compose.onAllNodesWithText("Direção").assertCountEquals(0)
    }

    @Test
    fun gestorVeInicioIdeiasProjetosDirecaoPerfilENaoVePainel() {
        renderizarBarra(Perfil.GESTOR)

        compose.onNodeWithText("Início").assertIsDisplayed()
        compose.onNodeWithText("Ideias").assertIsDisplayed()
        compose.onNodeWithText("Projetos").assertIsDisplayed()
        compose.onNodeWithText("Direção").assertIsDisplayed()
        compose.onNodeWithText("Perfil").assertIsDisplayed()
        compose.onAllNodesWithText("Painel").assertCountEquals(0)
        compose.onAllNodesWithText("Estratégia").assertCountEquals(0)
    }

    @Test
    fun liderancaVeInicioEstrategiaPainelProjetosPerfilENaoVeIdeias() {
        renderizarBarra(Perfil.LIDERANCA)

        compose.onNodeWithText("Início").assertIsDisplayed()
        compose.onNodeWithText("Estratégia").assertIsDisplayed()
        compose.onNodeWithText("Painel").assertIsDisplayed()
        compose.onNodeWithText("Projetos").assertIsDisplayed()
        compose.onNodeWithText("Perfil").assertIsDisplayed()
        compose.onAllNodesWithText("Ideias").assertCountEquals(0)
        compose.onAllNodesWithText("Direção").assertCountEquals(0)
    }
}
