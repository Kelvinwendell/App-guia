package br.com.fiap.aguiainova.core.session

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * A troca de servidor em tempo de execucao e um vetor de phishing em build de release (ver
 * comentario na interface ConfiguracaoServidor) - so deve ficar disponivel em debug.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ConfiguracaoServidorEditavelTest {

    private fun contexto() = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun ehEditavelQuandoConstruidoComEditavelTrue() {
        val servidor = ConfiguracaoServidorPrefs(contexto(), "http://padrao/", editavel = true)

        assertTrue(servidor.editavel())
    }

    @Test
    fun naoEhEditavelQuandoConstruidoComEditavelFalse() {
        val servidor = ConfiguracaoServidorPrefs(contexto(), "http://padrao/", editavel = false)

        assertFalse(servidor.editavel())
    }
}
