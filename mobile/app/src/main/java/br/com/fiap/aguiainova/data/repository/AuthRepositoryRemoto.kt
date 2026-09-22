package br.com.fiap.aguiainova.data.repository

import br.com.fiap.aguiainova.core.network.LeitorDeErroApi
import br.com.fiap.aguiainova.core.network.Resultado
import br.com.fiap.aguiainova.core.network.aoSucesso
import br.com.fiap.aguiainova.core.network.mapear
import br.com.fiap.aguiainova.core.session.GerenciadorSessao
import br.com.fiap.aguiainova.data.remote.api.AuthApi
import br.com.fiap.aguiainova.domain.model.Credenciais
import br.com.fiap.aguiainova.domain.model.NovoUsuario
import br.com.fiap.aguiainova.domain.model.Usuario
import br.com.fiap.aguiainova.domain.repository.AuthRepository
import kotlinx.coroutines.flow.StateFlow

class AuthRepositoryRemoto(
    private val api: AuthApi,
    private val sessao: GerenciadorSessao,
    leitorDeErro: LeitorDeErroApi
) : BaseRepositorio(leitorDeErro), AuthRepository {

    override val usuario: StateFlow<Usuario?> = sessao.usuario

    override suspend fun entrar(email: String, senha: String): Resultado<Usuario> =
        chamar { api.entrar(Credenciais(email.trim(), senha)) }
            .aoSucesso { sessao.iniciar(it.accessToken, it.usuario) }
            .mapear { it.usuario }

    override suspend fun cadastrar(novo: NovoUsuario): Resultado<Usuario> =
        chamar { api.cadastrar(novo.copy(email = novo.email.trim())) }

    override fun sair() = sessao.encerrar()
}
