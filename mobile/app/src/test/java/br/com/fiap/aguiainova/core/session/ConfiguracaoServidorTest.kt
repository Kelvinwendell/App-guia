package br.com.fiap.aguiainova.core.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ConfiguracaoServidorTest {

    @Test
    fun normalizaOEnderecoDoServidor() {
        assertEquals("http://192.168.0.10:8080/", ConfiguracaoServidorPrefs.normalizar("192.168.0.10:8080"))
        assertEquals("http://10.0.2.2:8080/", ConfiguracaoServidorPrefs.normalizar("  http://10.0.2.2:8080  "))
        assertEquals("https://api.exemplo.com/", ConfiguracaoServidorPrefs.normalizar("https://api.exemplo.com/"))
        assertEquals("https://api.exemplo.com/aguia/", ConfiguracaoServidorPrefs.normalizar("https://api.exemplo.com/aguia"))
    }

    @Test
    fun rejeitaEnderecosInvalidos() {
        assertNull(ConfiguracaoServidorPrefs.normalizar(""))
        assertNull(ConfiguracaoServidorPrefs.normalizar("   "))
        assertNull(ConfiguracaoServidorPrefs.normalizar("http://"))
        assertNull(ConfiguracaoServidorPrefs.normalizar("ftp://servidor"))
    }
}
