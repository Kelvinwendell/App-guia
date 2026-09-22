package br.com.fiap.aguiainova.core.session

import android.content.Context
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
 * Guarda a sessao no armazenamento privado do app (SharedPreferences), que outros apps nao conseguem ler. O arquivo
 * "sessao" e excluido do backup automatico do Android (veja res/xml), para o token nao sair do aparelho.
 */
class ArmazenamentoSessaoPrefs(context: Context) : ArmazenamentoSessao {

    private val preferencias = context.getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE)
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
