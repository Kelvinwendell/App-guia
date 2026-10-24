package br.com.fiap.aguiainova.ui.home

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import br.com.fiap.aguiainova.domain.model.Perfil
import br.com.fiap.aguiainova.ui.theme.AguiaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** SPEC-RD-004: cada home mostra so as acoes do seu perfil; cada clique chama o callback uma vez. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HomeConteudoTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun operadorVeApenasAsProprasAcoes() {
        compose.setContent {
            AguiaTheme {
                HomeConteudo(
                    perfil = Perfil.OPERADOR, resumo = ResumoHome(),
                    aoAbrirIdeias = {}, aoAbrirEstrategias = {}, aoAbrirProjetos = {},
                    aoCriarProjeto = {}, aoAbrirAssistente = {}, aoAbrirDashboard = {}
                )
            }
        }

        // "Minhas ideias" tambem e o rotulo do indicador; o subtitulo identifica o cartao de acao.
        compose.onNodeWithText("Cadastre e acompanhe o status").assertIsDisplayed()
        compose.onNodeWithText("Estratégia da empresa").assertIsDisplayed()
        compose.onAllNodesWithText("Assistente de IA").assertCountEquals(0)
        compose.onAllNodesWithText("Criar projeto").assertCountEquals(0)
    }

    @Test
    fun tocarAcaoPrimariaDoOperadorChamaOCallbackUmaVez() {
        var chamadas = 0
        compose.setContent {
            AguiaTheme {
                HomeConteudo(
                    perfil = Perfil.OPERADOR, resumo = ResumoHome(),
                    aoAbrirIdeias = { chamadas++ }, aoAbrirEstrategias = {}, aoAbrirProjetos = {},
                    aoCriarProjeto = {}, aoAbrirAssistente = {}, aoAbrirDashboard = {}
                )
            }
        }

        compose.onNodeWithText("Cadastre e acompanhe o status").performClick()

        assertEquals(1, chamadas)
    }

    @Test
    fun gestorVeAsCincoAcoesEsperadas() {
        compose.setContent {
            AguiaTheme {
                HomeConteudo(
                    perfil = Perfil.GESTOR, resumo = ResumoHome(),
                    aoAbrirIdeias = {}, aoAbrirEstrategias = {}, aoAbrirProjetos = {},
                    aoCriarProjeto = {}, aoAbrirAssistente = {}, aoAbrirDashboard = {}
                )
            }
        }

        // A lista rola: os ultimos cartoes ficam abaixo da dobra no viewport do Robolectric.
        compose.onNodeWithText("Aprovar ideias").assertExists()
        compose.onNodeWithText("Assistente de IA").assertExists()
        compose.onNodeWithText("Criar projeto").assertExists()
        compose.onNodeWithText("Atualizar progresso").assertExists()
        compose.onNodeWithText("Saúde das estratégias").assertExists()
    }

    @Test
    fun liderancaNaoVeAcoesDeOutrosPerfis() {
        compose.setContent {
            AguiaTheme {
                HomeConteudo(
                    perfil = Perfil.LIDERANCA, resumo = ResumoHome(),
                    aoAbrirIdeias = {}, aoAbrirEstrategias = {}, aoAbrirProjetos = {},
                    aoCriarProjeto = {}, aoAbrirAssistente = {}, aoAbrirDashboard = {}
                )
            }
        }

        compose.onNodeWithText("Painel executivo").assertIsDisplayed()
        compose.onAllNodesWithText("Aprovar ideias").assertCountEquals(0)
        compose.onAllNodesWithText("Minhas ideias").assertCountEquals(0)
    }
}
