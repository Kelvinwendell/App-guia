package br.com.fiap.aguiainova.ui.auth

import br.com.fiap.aguiainova.core.network.Resultado
import br.com.fiap.aguiainova.core.network.TipoErro
import br.com.fiap.aguiainova.core.session.ConfiguracaoServidor
import br.com.fiap.aguiainova.domain.model.NovoUsuario
import br.com.fiap.aguiainova.domain.model.Perfil
import br.com.fiap.aguiainova.domain.model.Usuario
import br.com.fiap.aguiainova.domain.repository.AuthRepository
import br.com.fiap.aguiainova.ui.support.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Regras de login/cadastro, testadas com um AuthRepository falso (sem rede) - o ViewModel so conhece a
 * interface de dominio.
 */
class AuthViewModelTest {

    @get:Rule
    val principal = MainDispatcherRule()

    private val auth = FakeAuth()
    private val servidor = FakeServidor()

    private fun viewModel() = AuthViewModel(auth, servidor)

    @Test
    fun `login com email ou senha em branco nao chama a API`() {
        val vm = viewModel()

        vm.entrar("", "123456") {}
        vm.entrar("a@b.com", "") {}

        assertEquals(0, auth.chamadasEntrar)
    }

    @Test
    fun `login com sucesso usa o perfil devolvido pelo servidor, nao um escolhido no app`() {
        auth.entrar = Resultado.Sucesso(usuario(Perfil.GESTOR))
        val vm = viewModel()
        var perfilRecebido: Perfil? = null

        vm.entrar("a@b.com", "123456") { perfilRecebido = it }

        assertEquals(Perfil.GESTOR, perfilRecebido)
    }

    @Test
    fun `falha no login avisa e nao chama o callback de sucesso`() {
        auth.entrar = Resultado.Falha(TipoErro.NAO_AUTENTICADO, "Credenciais inválidas.")
        val vm = viewModel()
        var chamado = false

        vm.entrar("a@b.com", "123456") { chamado = true }

        assertEquals("Credenciais inválidas.", vm.aviso.value)
        assertFalse(chamado)
    }

    @Test
    fun `cadastro com senha menor que 6 caracteres nao chama a API`() {
        val vm = viewModel()

        vm.cadastrar(novoUsuario(senha = "123"), {})

        assertEquals(0, auth.chamadasCadastrar)
    }

    @Test
    fun `cadastro com nome ou email em branco nao chama a API`() {
        val vm = viewModel()

        vm.cadastrar(novoUsuario(nome = ""), {})
        vm.cadastrar(novoUsuario(email = ""), {})

        assertEquals(0, auth.chamadasCadastrar)
    }

    @Test
    fun `cadastro valido chama a API e o callback de conclusao`() {
        auth.cadastrar = Resultado.Sucesso(usuario(Perfil.OPERADOR))
        val vm = viewModel()
        var chamado = false

        vm.cadastrar(novoUsuario(), { chamado = true })

        assertEquals(1, auth.chamadasCadastrar)
        assertTrue(chamado)
    }

    @Test
    fun `aviso lido some`() {
        val vm = viewModel()
        vm.entrar("", "x") {}

        vm.avisoLido()

        assertNull(vm.aviso.value)
    }

    // ---- apoio ----

    private fun usuario(perfil: Perfil) = Usuario(id = "1", nome = "Vinícius", email = "a@b.com", perfil = perfil)

    private fun novoUsuario(nome: String = "Vinícius", email: String = "a@b.com", senha: String = "123456") =
        NovoUsuario(nome, email, senha, Perfil.OPERADOR)

    @Test
    fun `segundo pedido de login enquanto o primeiro responde e ignorado`() {
        val segurar = CompletableDeferred<Resultado<Usuario>>()
        auth.segurar = segurar
        val vm = viewModel()

        vm.entrar("a@b.com", "123456") {}
        vm.entrar("a@b.com", "123456") {}

        assertEquals(1, auth.chamadasEntrar)
        segurar.complete(Resultado.Falha(TipoErro.NAO_AUTENTICADO, "Credenciais inválidas."))
        assertFalse(vm.ocupado.value)
    }

    private class FakeAuth : AuthRepository {
        override val usuario: StateFlow<Usuario?> = MutableStateFlow(null)
        var entrar: Resultado<Usuario> = Resultado.Falha(TipoErro.DESCONHECIDO, "não configurado no teste")
        var cadastrar: Resultado<Usuario> = Resultado.Falha(TipoErro.DESCONHECIDO, "não configurado no teste")
        var chamadasEntrar = 0
        var chamadasCadastrar = 0
        var segurar: CompletableDeferred<Resultado<Usuario>>? = null

        override suspend fun entrar(email: String, senha: String): Resultado<Usuario> {
            chamadasEntrar++
            return segurar?.await() ?: entrar
        }

        override suspend fun cadastrar(novo: NovoUsuario): Resultado<Usuario> {
            chamadasCadastrar++
            return cadastrar
        }

        override fun sair() {}
    }

    private class FakeServidor : ConfiguracaoServidor {
        private var url = "http://10.0.2.2:8080/"
        override fun urlBase() = url
        override fun definir(url: String): Boolean {
            this.url = url
            return true
        }
        override fun restaurarPadrao() {
            url = "http://10.0.2.2:8080/"
        }
        override fun editavel() = true
    }
}
