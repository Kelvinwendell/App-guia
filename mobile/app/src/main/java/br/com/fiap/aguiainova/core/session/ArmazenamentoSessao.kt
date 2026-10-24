package br.com.fiap.aguiainova.core.session

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import br.com.fiap.aguiainova.domain.model.Usuario
import com.google.gson.Gson

/** Onde a sessao (token JWT + usuario) fica guardada. Abstracao que permite trocar o meio nos testes. */
interface ArmazenamentoSessao {
    fun token(): String?
    fun usuario(): Usuario?
    fun salvar(token: String, usuario: Usuario)
    fun limpar()
}

/**
 * Guarda a sessao em SharedPreferences criptografado (Jetpack Security): nem copiando o arquivo de um aparelho
 * rooteado/com backup habilitado da para ler o token em texto puro. O arquivo "sessao" tambem e excluido do
 * backup automatico do Android (veja res/xml), como camada adicional.
 */
class ArmazenamentoSessaoPrefs(context: Context) : ArmazenamentoSessao {

    // "by lazy": construir o keystore/EncryptedSharedPreferences so no primeiro acesso de verdade, nao no
    // construtor. AppContainer e criado em Application.onCreate() para todo teste Robolectric (mesmo os que
    // nunca leem sessao) - e o Robolectric nao tem AndroidKeyStore real, entao construir isso de olho fechado
    // quebraria a suite inteira so por existir.
    private val preferencias by lazy {
        val chaveMestra = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        EncryptedSharedPreferences.create(
            context,
            ARQUIVO,
            chaveMestra,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    private val gson = Gson()

    override fun token(): String? = preferencias.getString(CHAVE_TOKEN, null)

    override fun usuario(): Usuario? = preferencias.getString(CHAVE_USUARIO, null)?.let {
        runCatching { gson.fromJson(it, Usuario::class.java) }.getOrNull()
    }

    override fun salvar(token: String, usuario: Usuario) {
        preferencias.edit()
            .putString(CHAVE_TOKEN, token)
            .putString(CHAVE_USUARIO, gson.toJson(usuario))
            .apply()
    }

    override fun limpar() {
        preferencias.edit().clear().apply()
    }

    private companion object {
        const val ARQUIVO = "sessao"
        const val CHAVE_TOKEN = "token"
        const val CHAVE_USUARIO = "usuario"
    }
}
