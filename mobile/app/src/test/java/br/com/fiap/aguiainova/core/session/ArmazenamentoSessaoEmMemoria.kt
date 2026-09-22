package br.com.fiap.aguiainova.core.session

import br.com.fiap.aguiainova.domain.model.Usuario

/** Armazenamento em memoria, usado nos testes no lugar das SharedPreferences. */
class ArmazenamentoSessaoEmMemoria(
    private var token: String? = null,
    private var usuario: Usuario? = null
) : ArmazenamentoSessao {
    override fun token() = token
    override fun usuario() = usuario
    override fun salvar(token: String, usuario: Usuario) {
        this.token = token
        this.usuario = usuario
    }
    override fun limpar() {
        token = null
        usuario = null
    }
}
