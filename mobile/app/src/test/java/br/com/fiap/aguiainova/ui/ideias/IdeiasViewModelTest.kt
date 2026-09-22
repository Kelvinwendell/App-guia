package br.com.fiap.aguiainova.ui.ideias

import br.com.fiap.aguiainova.core.network.Resultado
import br.com.fiap.aguiainova.core.network.TipoErro
import br.com.fiap.aguiainova.domain.model.AvaliacaoIdeia
import br.com.fiap.aguiainova.domain.model.AvaliacaoIdeiaIa
import br.com.fiap.aguiainova.domain.model.DadosIdeia
import br.com.fiap.aguiainova.domain.model.Ideia
import br.com.fiap.aguiainova.domain.model.InsightsIa
import br.com.fiap.aguiainova.domain.model.MensagemChat
import br.com.fiap.aguiainova.domain.model.Perfil
import br.com.fiap.aguiainova.domain.model.Prioridade
import br.com.fiap.aguiainova.domain.model.RankingIa
import br.com.fiap.aguiainova.domain.model.RespostaChat
import br.com.fiap.aguiainova.domain.model.StatusIa
import br.com.fiap.aguiainova.domain.model.StatusIdeia
import br.com.fiap.aguiainova.domain.repository.IaRepository
import br.com.fiap.aguiainova.domain.repository.IdeiaRepository
import br.com.fiap.aguiainova.ui.common.Estado
import br.com.fiap.aguiainova.ui.support.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Regras de apresentacao da lista de ideias, testadas com repositorios falsos (sem rede): o ViewModel so conhece as
 * interfaces de dominio, entao nada aqui depende de Retrofit, Android ou da API.
 */
class IdeiasViewModelTest {

    @get:Rule
    val principal = MainDispatcherRule()

    private val ideias = FakeIdeias()
    private val ia = FakeIa()

    private fun viewModel(perfil: Perfil = Perfil.GESTOR) = IdeiasViewModel(perfil, ideias, ia)

    @Test
    fun `carregar mostra as ideias devolvidas pela API`() {
        ideias.lista = Resultado.Sucesso(listOf(ideia("1"), ideia("2")))

        val vm = viewModel().also { it.carregar() }

        val estado = vm.estado.value as Estado.Pronto
        assertEquals(listOf("1", "2"), estado.dados.map { it.id })
    }

    @Test
    fun `falha na primeira carga vira tela de erro com a mensagem`() {
        ideias.lista = falha("Sem conexão com o servidor.")

        val vm = viewModel().also { it.carregar() }

        assertEquals(Estado.Erro("Sem conexão com o servidor."), vm.estado.value)
    }

    @Test
    fun `falha ao atualizar mantem a lista na tela e apenas avisa`() {
        ideias.lista = Resultado.Sucesso(listOf(ideia("1")))
        val vm = viewModel().also { it.carregar() }

        ideias.lista = falha("Servidor indisponível.")
        vm.carregar()

        assertTrue("os dados anteriores continuam visiveis", vm.estado.value is Estado.Pronto)
        assertEquals("Servidor indisponível.", vm.aviso.value)
    }

    @Test
    fun `filtrar por status consulta a API com esse status`() {
        val vm = viewModel().also { it.carregar() }

        vm.filtrarPor(StatusIdeia.EM_ANALISE)

        assertEquals(StatusIdeia.EM_ANALISE, ideias.ultimoFiltro)
        assertEquals(StatusIdeia.EM_ANALISE, vm.filtro.value)
    }

    @Test
    fun `operador alterna entre as proprias ideias e as aprovadas da empresa`() {
        ideias.lista = Resultado.Sucesso(listOf(ideia("minha")))
        ideias.aprovadas = Resultado.Sucesso(listOf(ideia("da-empresa", StatusIdeia.APROVADA)))
        val vm = viewModel(Perfil.OPERADOR).also { it.carregar() }
        assertEquals("minha", (vm.estado.value as Estado.Pronto).dados.single().id)

        vm.mostrarAprovadasDaEmpresa(true)

        assertEquals("da-empresa", (vm.estado.value as Estado.Pronto).dados.single().id)
    }

    @Test
    fun `priorizar com IA ordena pela nota e informa quantas ideias foram pontuadas`() {
        ia.ranking = Resultado.Sucesso(RankingIa(ranking = emptyList(), avaliadasAgora = 2))
        val vm = viewModel()

        vm.priorizarComIa()

        assertTrue(vm.ordenarPorIa.value)
        assertEquals("2 ideia(s) pontuada(s). Lista ordenada pela nota.", vm.aviso.value)
        assertEquals("a lista e recarregada com as notas novas", 1, ideias.consultas)
    }

    @Test
    fun `priorizar sem ideias novas para pontuar nao diz que 0 foram pontuadas`() {
        ia.ranking = Resultado.Sucesso(RankingIa(ranking = emptyList(), avaliadasAgora = 0))
        val vm = viewModel()

        vm.priorizarComIa()

        assertEquals("Lista ordenada pela nota da IA.", vm.aviso.value)
    }

    @Test
    fun `falha ao priorizar mostra o motivo e nao muda a ordem da lista`() {
        ia.ranking = falha("IA indisponível no momento.")
        val vm = viewModel()

        vm.priorizarComIa()

        assertEquals("IA indisponível no momento.", vm.aviso.value)
        assertFalse(vm.ordenarPorIa.value)
    }

    @Test
    fun `decidir avisa o novo status, recarrega e libera o indicador de ocupado`() {
        ideias.decisao = Resultado.Sucesso(ideia("1", StatusIdeia.APROVADA))
        val vm = viewModel()

        vm.decidir("1", AvaliacaoIdeia(StatusIdeia.APROVADA, Prioridade.ALTA, "Vamos em frente"))

        assertEquals("Ideia aprovada.", vm.aviso.value)
        assertEquals(1, ideias.consultas)
        assertFalse(vm.ocupado.value)
    }

    @Test
    fun `falha ao decidir mostra a mensagem, nao recarrega e libera o indicador de ocupado`() {
        ideias.decisao = falha("Você não tem permissão para esta ação.")
        val vm = viewModel()

        vm.decidir("1", AvaliacaoIdeia(StatusIdeia.REJEITADA, Prioridade.BAIXA, "Fora do escopo"))

        assertEquals("Você não tem permissão para esta ação.", vm.aviso.value)
        assertEquals(0, ideias.consultas)
        assertFalse(vm.ocupado.value)
    }

    @Test
    fun `aviso lido some`() {
        ideias.decisao = Resultado.Sucesso(ideia("1", StatusIdeia.REJEITADA))
        val vm = viewModel()
        vm.decidir("1", AvaliacaoIdeia(StatusIdeia.REJEITADA, Prioridade.BAIXA, "x"))

        vm.avisoLido()

        assertNull(vm.aviso.value)
    }

    // ---- apoio ----

    private fun ideia(id: String, status: StatusIdeia = StatusIdeia.EM_ANALISE) = Ideia(
        id = id,
        titulo = "Ideia $id",
        descricao = "Descrição da ideia $id",
        categoria = "Tecnologia",
        status = status,
        prioridade = Prioridade.MEDIA,
        autorId = "autor-1",
        autorNome = "Vinícius"
    )

    private fun falha(mensagem: String) = Resultado.Falha(TipoErro.SERVIDOR, mensagem)

    private class FakeIdeias : IdeiaRepository {
        var lista: Resultado<List<Ideia>> = Resultado.Sucesso(emptyList())
        var aprovadas: Resultado<List<Ideia>> = Resultado.Sucesso(emptyList())
        var decisao: Resultado<Ideia> = Resultado.Falha(TipoErro.DESCONHECIDO, "não configurado no teste")
        var ultimoFiltro: StatusIdeia? = null
        var consultas = 0

        override suspend fun listar(status: StatusIdeia?, prioridade: Prioridade?): Resultado<List<Ideia>> {
            consultas++
            ultimoFiltro = status
            return lista
        }

        override suspend fun aprovadas() = aprovadas.also { consultas++ }

        override suspend fun avaliar(id: String, avaliacao: AvaliacaoIdeia) = decisao

        override suspend fun obter(id: String) = naoUsado<Ideia>()

        override suspend fun criar(dados: DadosIdeia) = naoUsado<Ideia>()

        override suspend fun atualizar(id: String, dados: DadosIdeia) = naoUsado<Ideia>()

        override suspend fun remover(id: String) = naoUsado<Unit>()
    }

    private class FakeIa : IaRepository {
        var ranking: Resultado<RankingIa> = Resultado.Falha(TipoErro.DESCONHECIDO, "não configurado no teste")

        override suspend fun avaliarPendentes() = ranking

        override suspend fun status() = naoUsado<StatusIa>()

        override suspend fun avaliarIdeia(ideiaId: String) = naoUsado<AvaliacaoIdeiaIa>()

        override suspend fun conversar(mensagem: String, historico: List<MensagemChat>) = naoUsado<RespostaChat>()

        override suspend fun insights() = naoUsado<InsightsIa>()
    }

    private companion object {
        fun <T> naoUsado(): Resultado<T> = Resultado.Falha(TipoErro.DESCONHECIDO, "não usado neste teste")
    }
}
