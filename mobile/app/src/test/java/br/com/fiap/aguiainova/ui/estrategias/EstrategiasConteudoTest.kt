package br.com.fiap.aguiainova.ui.estrategias

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import br.com.fiap.aguiainova.domain.model.Estrategia
import br.com.fiap.aguiainova.domain.model.NivelSaude
import br.com.fiap.aguiainova.domain.model.Perfil
import br.com.fiap.aguiainova.domain.model.SaudeEstrategia
import br.com.fiap.aguiainova.domain.model.StatusEstrategia
import br.com.fiap.aguiainova.ui.common.Estado
import br.com.fiap.aguiainova.ui.theme.AguiaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * SPEC-RD-007: vigentes e encerradas nao se misturam, o resumo de saude so aparece nas vigentes, campanha ausente
 * nao vira "null", o vazio orienta conforme o perfil e o erro oferece tentar de novo.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class EstrategiasConteudoTest {

    @get:Rule
    val compose = createComposeRule()

    private val vigente = Estrategia("a", "Eficiência energética", "Reduzir consumo.", "Sustentabilidade", null, StatusEstrategia.VIGENTE)
    private val encerrada = Estrategia("b", "Plano antigo", "Encerrado.", "Qualidade", "Campanha 2025", StatusEstrategia.ENCERRADA)
    private val saude = SaudeEstrategia(
        estrategiaId = "a", titulo = "Eficiência energética", nivel = NivelSaude.EM_RISCO, proximaAcao = "Criar projetos.",
        ideias = 2, ideiasEmAnalise = 1, aprovadasSemProjeto = 1, projetos = 0, projetosAtrasados = 0, investimento = 0.0,
        retorno = 0.0
    )

    private fun lista(
        estado: Estado<PainelEstrategias>,
        vigentes: Boolean = true,
        perfil: Perfil = Perfil.LIDERANCA,
        aoTentarNovamente: () -> Unit = {},
        aoCriar: () -> Unit = {}
    ) = compose.setContent {
        AguiaTheme {
            ListaDeEstrategias(estado, vigentes, perfil, aoTentarNovamente, aoAbrir = {}, aoCriar = aoCriar, modifier = Modifier)
        }
    }

    @Test
    fun vigentesMostramResumoDeSaudeESoAsVigentes() {
        lista(Estado.Pronto(PainelEstrategias(listOf(vigente, encerrada), mapOf("a" to saude))))

        compose.onNodeWithText("Eficiência energética").assertExists()
        compose.onAllNodesWithText("Plano antigo").assertCountEquals(0)
        compose.onNodeWithText("1 ${NivelSaude.EM_RISCO.rotulo.lowercase()}").assertExists()
    }

    @Test
    fun encerradasNaoMostramResumoDeSaude() {
        lista(Estado.Pronto(PainelEstrategias(listOf(vigente, encerrada), mapOf("a" to saude))), vigentes = false)

        compose.onNodeWithText("Plano antigo").assertExists()
        compose.onNodeWithText("Campanha 2025").assertExists()
        compose.onAllNodesWithText("1 ${NivelSaude.EM_RISCO.rotulo.lowercase()}").assertCountEquals(0)
    }

    @Test
    fun campanhaAusenteNaoViraTextoNull() {
        lista(Estado.Pronto(PainelEstrategias(listOf(vigente))))

        compose.onAllNodesWithText("null", substring = true).assertCountEquals(0)
    }

    @Test
    fun liderancaSemEstrategiasRecebeAcaoDeCriar() {
        var criacoes = 0
        lista(Estado.Pronto(PainelEstrategias(emptyList())), aoCriar = { criacoes++ })

        compose.onNodeWithText("Criar estratégia").performClick()

        assertEquals(1, criacoes)
    }

    @Test
    fun operadorSemEstrategiasNaoRecebeAcaoDeCriar() {
        lista(Estado.Pronto(PainelEstrategias(emptyList())), perfil = Perfil.OPERADOR)

        compose.onAllNodesWithText("Criar estratégia").assertCountEquals(0)
    }

    @Test
    fun erroOfereceTentarDeNovo() {
        var tentativas = 0
        lista(Estado.Erro("Sem conexão com o servidor."), aoTentarNovamente = { tentativas++ })

        compose.onNodeWithText("Tentar novamente").performClick()

        assertEquals(1, tentativas)
    }
}
