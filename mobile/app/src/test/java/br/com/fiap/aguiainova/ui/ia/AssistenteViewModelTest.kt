package br.com.fiap.aguiainova.ui.ia

import br.com.fiap.aguiainova.core.network.Resultado
import br.com.fiap.aguiainova.core.network.TipoErro
import br.com.fiap.aguiainova.domain.model.AnaliseEstrategia
import br.com.fiap.aguiainova.domain.model.AvaliacaoIdeiaIa
import br.com.fiap.aguiainova.domain.model.DadosEstrategia
import br.com.fiap.aguiainova.domain.model.InsightsIa
import br.com.fiap.aguiainova.domain.model.LeituraSaude
import br.com.fiap.aguiainova.domain.model.MensagemChat
import br.com.fiap.aguiainova.domain.model.PapelMensagem
import br.com.fiap.aguiainova.domain.model.RankingIa
import br.com.fiap.aguiainova.domain.model.RascunhoEstrategia
import br.com.fiap.aguiainova.domain.model.RespostaChat
import br.com.fiap.aguiainova.domain.model.StatusIa
import br.com.fiap.aguiainova.domain.repository.IaRepository
import br.com.fiap.aguiainova.ui.support.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * SPEC-RD-009: pergunta vazia nao chama a IA, o historico enviado e o anterior limitado a 20, falha nao inventa
 * resposta, duplo envio faz uma chamada so e resposta por regras e marcada como tal.
 */
class AssistenteViewModelTest {

    @get:Rule
    val principal = MainDispatcherRule()

    private val ia = FakeIa()

    @Test
    fun `mensagem em branco nao chama a IA`() {
        val vm = AssistenteViewModel(ia)

        vm.enviar("   ")

        assertTrue(ia.conversas.isEmpty())
        assertTrue(vm.mensagens.value.isEmpty())
    }

    @Test
    fun `envio leva a pergunta e o historico anterior`() {
        val vm = AssistenteViewModel(ia)
        vm.enviar("primeira")

        vm.enviar(" segunda ")

        val (pergunta, historico) = ia.conversas.last()
        assertEquals("segunda", pergunta)
        assertEquals(listOf("primeira", "resposta 1"), historico.map { it.texto })
    }

    @Test
    fun `historico enviado e limitado a 20 mensagens`() {
        val vm = AssistenteViewModel(ia)
        repeat(15) { vm.enviar("pergunta $it") }

        vm.enviar("ultima")

        val historico = ia.conversas.last().second
        assertEquals(20, historico.size)
        assertEquals("resposta 15", historico.last().texto)
    }

    @Test
    fun `falha nao cria resposta do assistente e mantem a pergunta visivel`() {
        ia.resposta = { Resultado.Falha(TipoErro.REDE, "Sem conexão com o servidor.") }
        val vm = AssistenteViewModel(ia)

        vm.enviar("vai falhar")

        assertEquals(listOf(PapelMensagem.USUARIO), vm.mensagens.value.map { it.papel })
        assertEquals("vai falhar", vm.mensagens.value.single().texto)
        assertEquals("Sem conexão com o servidor.", vm.aviso.value)
    }

    @Test
    fun `duplo envio durante uma resposta faz uma chamada so`() {
        val segurar = CompletableDeferred<Resultado<RespostaChat>>()
        ia.respostaSuspensa = segurar
        val vm = AssistenteViewModel(ia)

        vm.enviar("uma")
        vm.enviar("duas")

        assertEquals(1, ia.conversas.size)
        assertEquals(1, vm.mensagens.value.size)
        segurar.complete(Resultado.Sucesso(RespostaChat("ok", "GEMINI")))
        assertFalse(vm.ocupado.value)
    }

    @Test
    fun `resposta heuristica fica marcada como leitura por regras`() {
        ia.resposta = { Resultado.Sucesso(RespostaChat("por regras", "HEURISTICA")) }
        val vm = AssistenteViewModel(ia)

        vm.enviar("pergunta")

        assertEquals(setOf(1), vm.respostasPorRegras.value)
    }

    @Test
    fun `status que nao carregou nao parece conectado`() {
        ia.status = Resultado.Falha(TipoErro.REDE, "Sem conexão com o servidor.")
        val vm = AssistenteViewModel(ia)

        vm.carregarStatus()

        assertEquals(null, vm.status.value)
        assertTrue(vm.statusVerificado.value)
    }

    private class FakeIa : IaRepository {
        val conversas = mutableListOf<Pair<String, List<MensagemChat>>>()
        var resposta: (Int) -> Resultado<RespostaChat> = { Resultado.Sucesso(RespostaChat("resposta $it", "GEMINI")) }
        var respostaSuspensa: CompletableDeferred<Resultado<RespostaChat>>? = null
        var status: Resultado<StatusIa> = Resultado.Sucesso(StatusIa(true, true, "GEMINI", "modelo"))

        override suspend fun conversar(mensagem: String, historico: List<MensagemChat>): Resultado<RespostaChat> {
            conversas += mensagem to historico
            return respostaSuspensa?.await() ?: resposta(conversas.size)
        }

        override suspend fun status() = status

        override suspend fun avaliarIdeia(ideiaId: String) = naoUsado<AvaliacaoIdeiaIa>()

        override suspend fun avaliarPendentes() = naoUsado<RankingIa>()

        override suspend fun insights() = naoUsado<InsightsIa>()

        override suspend fun rascunharEstrategia(objetivo: String) = naoUsado<RascunhoEstrategia>()

        override suspend fun analisarEstrategia(estrategiaId: String?, dados: DadosEstrategia) = naoUsado<AnaliseEstrategia>()

        override suspend fun lerSaude(estrategiaId: String) = naoUsado<LeituraSaude>()

        private fun <T> naoUsado(): Resultado<T> = Resultado.Falha(TipoErro.DESCONHECIDO, "não usado neste teste")
    }
}
