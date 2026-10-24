package br.com.fiap.aguiainova.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import br.com.fiap.aguiainova.domain.model.Prioridade
import br.com.fiap.aguiainova.domain.model.StatusEstrategia
import br.com.fiap.aguiainova.domain.model.StatusIdeia
import br.com.fiap.aguiainova.domain.model.StatusProjeto
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Cor de cada status/prioridade do dominio (CoresDeDominio.kt). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CoresDeDominioTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun statusIdeiaUsaCoresDeMarca() {
        var aprovada: Color? = null
        var rejeitada: Color? = null
        var emAnalise: Color? = null
        compose.setContent {
            AguiaTheme {
                aprovada = StatusIdeia.APROVADA.cor()
                rejeitada = StatusIdeia.REJEITADA.cor()
                emAnalise = StatusIdeia.EM_ANALISE.cor()
            }
        }
        compose.waitForIdle()

        assertEquals(AguiaCores.Sucesso, aprovada)
        assertEquals(AguiaCores.Erro, rejeitada)
        assertEquals(AguiaCores.Alerta, emAnalise)
    }

    @Test
    fun prioridadeUsaCoresDeMarca() {
        var alta: Color? = null
        var media: Color? = null
        var baixa: Color? = null
        compose.setContent {
            AguiaTheme {
                alta = Prioridade.ALTA.cor()
                media = Prioridade.MEDIA.cor()
                baixa = Prioridade.BAIXA.cor()
            }
        }
        compose.waitForIdle()

        assertEquals(AguiaCores.Erro, alta)
        assertEquals(AguiaCores.Alerta, media)
        assertEquals(AguiaCores.Sucesso, baixa)
    }

    @Test
    fun statusEstrategiaUsaCorDeMarcaOuTextoSecundario() {
        var vigente: Color? = null
        var encerrada: Color? = null
        compose.setContent {
            AguiaTheme {
                vigente = StatusEstrategia.VIGENTE.cor()
                encerrada = StatusEstrategia.ENCERRADA.cor()
            }
        }
        compose.waitForIdle()

        assertEquals(AguiaCores.Sucesso, vigente)
        assertEquals(AguiaCores.TextoSecundario, encerrada)
    }

    @Test
    fun statusProjetoUsaCorDeMarcaOuTextoSecundario() {
        var emAndamento: Color? = null
        var concluido: Color? = null
        var planejamento: Color? = null
        var pausado: Color? = null
        var cancelado: Color? = null
        compose.setContent {
            AguiaTheme {
                emAndamento = StatusProjeto.EM_ANDAMENTO.cor()
                concluido = StatusProjeto.CONCLUIDO.cor()
                planejamento = StatusProjeto.PLANEJAMENTO.cor()
                pausado = StatusProjeto.PAUSADO.cor()
                cancelado = StatusProjeto.CANCELADO.cor()
            }
        }
        compose.waitForIdle()

        assertEquals(AguiaCores.Primaria, emAndamento)
        assertEquals(AguiaCores.Sucesso, concluido)
        assertEquals(AguiaCores.Alerta, planejamento)
        assertEquals(AguiaCores.TextoSecundario, pausado)
        assertEquals(AguiaCores.Erro, cancelado)
    }
}
