package br.com.fiap.aguiainova.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Datas e numeros digitados: o que nao existe e recusado (nunca "ajustado" em silencio para outra coisa). */
class FormatadoresTest {

    @Test
    fun `data valida vira ISO`() {
        assertEquals("2027-02-28", Formatadores.dataIsoDeDigitada("28/02/2027"))
    }

    @Test
    fun `dia que nao existe no mes e recusado em vez de virar o ultimo dia`() {
        assertNull(Formatadores.dataIsoDeDigitada("31/02/2027"))
        assertNull(Formatadores.dataIsoDeDigitada("31/04/2027"))
    }

    @Test
    fun `29 de fevereiro so existe em ano bissexto`() {
        assertEquals("2028-02-29", Formatadores.dataIsoDeDigitada("29/02/2028"))
        assertNull(Formatadores.dataIsoDeDigitada("29/02/2027"))
    }

    @Test
    fun `formato errado e recusado`() {
        assertNull(Formatadores.dataIsoDeDigitada("2027-02-10"))
        assertNull(Formatadores.dataIsoDeDigitada("10/13/2027"))
    }

    @Test
    fun `espacos em volta sao ignorados`() {
        assertEquals("2027-03-01", Formatadores.dataIsoDeDigitada(" 01/03/2027 "))
    }

    @Test
    fun `numero em formato brasileiro e convertido`() {
        assertEquals(1234.56, Formatadores.numeroDigitado("1.234,56")!!, 0.0001)
    }
}
