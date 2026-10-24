package br.com.fiap.aguiainova.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import br.com.fiap.aguiainova.domain.model.Prioridade
import br.com.fiap.aguiainova.domain.model.StatusEstrategia
import br.com.fiap.aguiainova.domain.model.StatusIdeia
import br.com.fiap.aguiainova.domain.model.StatusProjeto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Cor de cada status/prioridade do dominio (CoresDeDominio.kt). Os casos "neutros" (Estrategia
 * Encerrada, Projeto Pausado) passaram a ler MaterialTheme.colorScheme.onSurfaceVariant para
 * acompanhar o tema claro/escuro, em vez da cor fixa que so fazia sentido no tema escuro original.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CoresDeDominioTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun statusIdeiaUsaCoresDeMarcaFixas() {
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
    fun prioridadeUsaCoresDeMarcaFixas() {
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
    fun statusEstrategiaVigenteUsaCorDeMarcaFixaNosDoisTemas() {
        var vigenteClaro: Color? = null
        var vigenteEscuro: Color? = null
        compose.setContent {
            Column {
                AguiaTheme(escuroForcado = false) { vigenteClaro = StatusEstrategia.VIGENTE.cor() }
                AguiaTheme(escuroForcado = true) { vigenteEscuro = StatusEstrategia.VIGENTE.cor() }
            }
        }
        compose.waitForIdle()

        assertEquals(AguiaCores.Sucesso, vigenteClaro)
        assertEquals(AguiaCores.Sucesso, vigenteEscuro)
    }

    @Test
    fun statusEstrategiaEncerradaAcompanhaOTema() {
        var encerradaClara: Color? = null
        var encerradaEscura: Color? = null
        compose.setContent {
            Column {
                AguiaTheme(escuroForcado = false) { encerradaClara = StatusEstrategia.ENCERRADA.cor() }
                AguiaTheme(escuroForcado = true) { encerradaEscura = StatusEstrategia.ENCERRADA.cor() }
            }
        }
        compose.waitForIdle()

        assertEquals(NeutrosClaro.TextoSecundario, encerradaClara)
        assertEquals(NeutrosEscuro.TextoSecundario, encerradaEscura)
        assertNotEquals(encerradaClara, encerradaEscura)
    }

    @Test
    fun statusProjetoPausadoAcompanhaOTema() {
        var pausadoClaro: Color? = null
        var pausadoEscuro: Color? = null
        compose.setContent {
            Column {
                AguiaTheme(escuroForcado = false) { pausadoClaro = StatusProjeto.PAUSADO.cor() }
                AguiaTheme(escuroForcado = true) { pausadoEscuro = StatusProjeto.PAUSADO.cor() }
            }
        }
        compose.waitForIdle()

        assertEquals(NeutrosClaro.TextoSecundario, pausadoClaro)
        assertEquals(NeutrosEscuro.TextoSecundario, pausadoEscuro)
        assertNotEquals(pausadoClaro, pausadoEscuro)
    }

    @Test
    fun statusProjetoUsaCoresDeMarcaFixasNosOutrosCasos() {
        var emAndamento: Color? = null
        var concluido: Color? = null
        var planejamento: Color? = null
        var cancelado: Color? = null
        compose.setContent {
            AguiaTheme {
                emAndamento = StatusProjeto.EM_ANDAMENTO.cor()
                concluido = StatusProjeto.CONCLUIDO.cor()
                planejamento = StatusProjeto.PLANEJAMENTO.cor()
                cancelado = StatusProjeto.CANCELADO.cor()
            }
        }
        compose.waitForIdle()

        assertEquals(AguiaCores.Primaria, emAndamento)
        assertEquals(AguiaCores.Sucesso, concluido)
        assertEquals(AguiaCores.Alerta, planejamento)
        assertEquals(AguiaCores.Erro, cancelado)
    }
}
