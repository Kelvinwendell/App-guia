package br.com.fiap.aguiainova.ui.auth

import br.com.fiap.aguiainova.core.session.ConfiguracaoServidor
import br.com.fiap.aguiainova.domain.model.NovoUsuario
import br.com.fiap.aguiainova.domain.model.Perfil
import br.com.fiap.aguiainova.domain.repository.AuthRepository
import br.com.fiap.aguiainova.ui.common.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel(
    private val auth: AuthRepository,
    private val servidor: ConfiguracaoServidor
) : BaseViewModel() {

    private val _urlServidor = MutableStateFlow(servidor.urlBase())
    val urlServidor: StateFlow<String> = _urlServidor.asStateFlow()

    fun entrar(email: String, senha: String, aoEntrar: (Perfil) -> Unit) {
        if (email.isBlank() || senha.isBlank()) {
            avisar("Informe e-mail e senha.")
            return
        }
        executar({ auth.entrar(email, senha) }) { aoEntrar(it.perfil) }
    }

    fun cadastrar(novo: NovoUsuario, aoCadastrar: () -> Unit) {
        val dadosValidos = novo.nome.isNotBlank() && novo.email.isNotBlank() && novo.senha.length >= TAMANHO_MINIMO_SENHA
        if (!dadosValidos) {
            avisar("Preencha nome, e-mail e uma senha com ao menos $TAMANHO_MINIMO_SENHA caracteres.")
            return
        }
        executar({ auth.cadastrar(novo) }) { aoCadastrar() }
    }

    fun salvarServidor(url: String): Boolean {
        val salvo = servidor.definir(url)
        if (salvo) _urlServidor.value = servidor.urlBase() else avisar("Endereço de servidor inválido.")
        return salvo
    }

    fun restaurarServidor() {
        servidor.restaurarPadrao()
        _urlServidor.value = servidor.urlBase()
    }

    private companion object {
        const val TAMANHO_MINIMO_SENHA = 6
    }
}
