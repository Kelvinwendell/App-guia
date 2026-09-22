package br.com.fiap.aguiainova.core.session

import br.com.fiap.aguiainova.domain.model.Perfil
import br.com.fiap.aguiainova.domain.model.Usuario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class GerenciadorSessaoTest {

    private val usuario = Usuario("1", "Ana", "ana@teste.com", Perfil.GESTOR)

    @Test
    fun iniciarGuardaOTokenEPublicaOUsuario() {
        val sessao = GerenciadorSessao(ArmazenamentoSessaoEmMemoria())

        sessao.iniciar("token-1", usuario)

        assertEquals("token-1", sessao.token())
        assertEquals(usuario, sessao.usuario.value)
    }

    @Test
    fun encerrarLimpaTudoEAvisaAInterface() {
        val sessao = GerenciadorSessao(ArmazenamentoSessaoEmMemoria("t", usuario))

        sessao.encerrar()

        assertNull(sessao.token())
        assertNull(sessao.usuario.value)
    }

    @Test
    fun restauraASessaoSalvaAoIniciarOApp() {
        val sessao = GerenciadorSessao(ArmazenamentoSessaoEmMemoria("t", usuario))

        assertNotNull(sessao.usuario.value)
        assertEquals(Perfil.GESTOR, sessao.usuario.value?.perfil)
    }

    @Test
    fun dadosIncompletosContamComoSemSessao() {
        assertNull(GerenciadorSessao(ArmazenamentoSessaoEmMemoria(token = null, usuario = usuario)).usuario.value)
        assertNull(GerenciadorSessao(ArmazenamentoSessaoEmMemoria(token = "t", usuario = null)).usuario.value)
    }
}
