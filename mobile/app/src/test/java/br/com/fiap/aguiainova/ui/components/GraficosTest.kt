package br.com.fiap.aguiainova.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * SPEC-RD-008: valor zero nao pode desenhar barra visivel (o minimo de MENOR_FRACAO
 * e so para valores reais pequenos, nao para ausencia de dado).
 */
class GraficosTest {

    @Test
    fun `valor zero resulta em fracao zero`() {
        assertEquals(0f, fracaoDaBarra(0.0, maximo = 100.0))
    }

    @Test
    fun `valor pequeno mas diferente de zero respeita o minimo visivel`() {
        val fracao = fracaoDaBarra(0.001, maximo = 1000.0)

        assertEquals(0.02f, fracao)
    }

    @Test
    fun `valor igual ao maximo preenche totalmente`() {
        assertEquals(1f, fracaoDaBarra(50.0, maximo = 50.0))
    }

    @Test
    fun `valor negativo usa o modulo para a fracao`() {
        assertEquals(0.5f, fracaoDaBarra(-25.0, maximo = 50.0))
    }
}
