package br.com.fiap.aguiainova.core.session

import android.content.Context
import br.com.fiap.aguiainova.BuildConfig
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/** Endereco do servidor da API, editavel em tempo de execucao (aparelho fisico x emulador). */
interface ConfiguracaoServidor {
    fun urlBase(): String

    /** Salva o endereco se ele for valido (http/https); devolve falso quando nao for. */
    fun definir(url: String): Boolean

    fun restaurarPadrao()

    /**
     * Se a troca de servidor deve ficar disponivel na UI. Em build de release isso e um vetor de
     * phishing (um app malicioso ou aparelho comprometido pode reapontar o app para um backend falso
     * e capturar credenciais pela tela de login legitima) - so faz sentido em build de debug.
     */
    fun editavel(): Boolean
}

class ConfiguracaoServidorPrefs(
    context: Context,
    private val urlPadrao: String,
    private val editavel: Boolean = BuildConfig.DEBUG
) : ConfiguracaoServidor {

    private val preferencias = context.getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE)

    override fun urlBase(): String = preferencias.getString(CHAVE_URL, null) ?: urlPadrao

    override fun definir(url: String): Boolean {
        val normalizada = normalizar(url) ?: return false
        preferencias.edit().putString(CHAVE_URL, normalizada).apply()
        return true
    }

    override fun restaurarPadrao() {
        preferencias.edit().remove(CHAVE_URL).apply()
    }

    override fun editavel(): Boolean = editavel

    companion object {
        private const val ARQUIVO = "servidor"
        private const val CHAVE_URL = "url_base"

        /** Aceita "192.168.0.10:8080" (assume http) e garante a barra final; nulo se a URL for invalida. */
        fun normalizar(entrada: String): String? {
            val texto = entrada.trim()
            if (texto.isEmpty()) return null
            val temEsquemaHttp = texto.startsWith("http://") || texto.startsWith("https://")
            if (!temEsquemaHttp && texto.contains("://")) return null // outro protocolo (ex.: ftp://)
            val comEsquema = if (temEsquemaHttp) texto else "http://$texto"
            val url = comEsquema.toHttpUrlOrNull() ?: return null
            return url.toString().let { if (it.endsWith("/")) it else "$it/" }
        }
    }
}
