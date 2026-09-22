package br.com.fiap.aguiainova.core.session

import br.com.fiap.aguiainova.domain.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Fonte unica da verdade sobre quem esta logado. A interface observa [usuario]: quando ele vira nulo (logout ou token
 * expirado) o app volta sozinho para a tela de login.
 */
class GerenciadorSessao(private val armazenamento: ArmazenamentoSessao) {

    private val _usuario = MutableStateFlow(usuarioSalvo())
    val usuario: StateFlow<Usuario?> = _usuario.asStateFlow()

    fun token(): String? = armazenamento.token()

    fun iniciar(token: String, usuario: Usuario) {
        armazenamento.salvar(token, usuario)
        _usuario.value = usuario
    }

    fun encerrar() {
        armazenamento.limpar()
        _usuario.value = null
    }

    /** So considera logado quem tem token e usuario salvos; qualquer dado incompleto vira sessao encerrada. */
    private fun usuarioSalvo(): Usuario? =
        if (armazenamento.token() != null) armazenamento.usuario() else null
}
