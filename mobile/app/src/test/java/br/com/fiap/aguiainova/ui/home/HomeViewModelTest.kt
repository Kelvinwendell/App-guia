package br.com.fiap.aguiainova.ui.home

import br.com.fiap.aguiainova.core.network.Resultado
import br.com.fiap.aguiainova.core.network.TipoErro
import br.com.fiap.aguiainova.domain.model.AvaliacaoIdeia
import br.com.fiap.aguiainova.domain.model.DadosIdeia
import br.com.fiap.aguiainova.domain.model.DadosProjeto
import br.com.fiap.aguiainova.domain.model.AndamentoProjeto
import br.com.fiap.aguiainova.domain.model.DadosResultado
import br.com.fiap.aguiainova.domain.model.DesempenhoEstrategia
import br.com.fiap.aguiainova.domain.model.DesempenhoProjeto
import br.com.fiap.aguiainova.domain.model.EtapaProjeto
import br.com.fiap.aguiainova.domain.model.EvolucaoMensal
import br.com.fiap.aguiainova.domain.model.Ideia
import br.com.fiap.aguiainova.domain.model.Perfil
import br.com.fiap.aguiainova.domain.model.Prioridade
import br.com.fiap.aguiainova.domain.model.Projeto
import br.com.fiap.aguiainova.domain.model.ResumoDashboard
import br.com.fiap.aguiainova.domain.model.ResumoIdeias
import br.com.fiap.aguiainova.domain.model.StatusIdeia
import br.com.fiap.aguiainova.domain.model.StatusProjeto
import br.com.fiap.aguiainova.domain.repository.DashboardRepository
import br.com.fiap.aguiainova.domain.repository.IdeiaRepository
import br.com.fiap.aguiainova.domain.repository.ProjetoRepository
import br.com.fiap.aguiainova.ui.common.Estado
import br.com.fiap.aguiainova.ui.support.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** SPEC-RD-004: cada perfil resume so o que mostra na home, testado com repositorios falsos. */
class HomeViewModelTest {

    @get:Rule
    val principal = MainDispatcherRule()

    private val ideias = FakeIdeias()
    private val projetos = FakeProjetos()
    private val dashboard = FakeDashboard()

    private fun viewModel(perfil: Perfil) = HomeViewModel(perfil, ideias, projetos, dashboard)

    @Test
    fun `operador consulta so as proprias ideias e conta as aprovadas`() {
        ideias.lista = Resultado.Sucesso(
            listOf(ideia(StatusIdeia.APROVADA), ideia(StatusIdeia.APROVADA), ideia(StatusIdeia.EM_ANALISE))
        )

        val vm = viewModel(Perfil.OPERADOR).also { it.carregar() }

        val resumo = (vm.estado.value as Estado.Pronto).dados
        assertEquals(3, resumo.ideiasTotal)
        assertEquals(2, resumo.ideiasAprovadas)
    }

    @Test
    fun `gestor combina ideias pendentes e projetos ativos e soma o retorno`() {
        ideias.lista = Resultado.Sucesso(listOf(ideia(StatusIdeia.EM_ANALISE), ideia(StatusIdeia.EM_ANALISE)))
        projetos.lista = Resultado.Sucesso(
            listOf(
                projeto(StatusProjeto.EM_ANDAMENTO, retorno = 1000.0),
                projeto(StatusProjeto.CONCLUIDO, retorno = 500.0),
                projeto(StatusProjeto.EM_ANDAMENTO, retorno = 2000.0)
            )
        )

        val vm = viewModel(Perfil.GESTOR).also { it.carregar() }

        val resumo = (vm.estado.value as Estado.Pronto).dados
        assertEquals(2, resumo.ideiasPendentes)
        assertEquals(2, resumo.projetosAtivos)
        assertEquals(3500.0, resumo.retornoParcial, 0.01)
    }

    @Test
    fun `lideranca consulta o resumo do dashboard`() {
        dashboard.resumo = Resultado.Sucesso(resumoDashboard(roi = 12.5))

        val vm = viewModel(Perfil.LIDERANCA).also { it.carregar() }

        val resumo = (vm.estado.value as Estado.Pronto).dados
        assertEquals(12.5, resumo.dashboard?.roiPercentual)
    }

    @Test
    fun `falha ao carregar vira erro com a mensagem`() {
        ideias.lista = Resultado.Falha(TipoErro.SERVIDOR, "Servidor indisponível.")

        val vm = viewModel(Perfil.OPERADOR).also { it.carregar() }

        assertEquals(Estado.Erro("Servidor indisponível."), vm.estado.value)
    }

    // ---- apoio ----

    private fun ideia(status: StatusIdeia) = Ideia(
        id = "1", titulo = "Ideia", descricao = "Descrição", categoria = "Tecnologia",
        status = status, prioridade = Prioridade.MEDIA, autorId = "a", autorNome = "Vinícius"
    )

    private fun projeto(status: StatusProjeto, retorno: Double) = Projeto(
        id = "1", nome = "Projeto", descricao = "Descrição", status = status, etapa = EtapaProjeto.EXECUCAO,
        investimento = 1000.0, prazo = "2026-12-31", progresso = 10, retornoFinanceiro = retorno
    )

    private fun resumoDashboard(roi: Double) = ResumoDashboard(
        investimentoTotal = 1000.0, retornoTotal = 1500.0, lucroTotal = 500.0, roiPercentual = roi,
        totalProjetos = 5, projetosAtrasados = 1, ideias = ResumoIdeias(10, 2, 7, 1)
    )

    private class FakeIdeias : IdeiaRepository {
        var lista: Resultado<List<Ideia>> = Resultado.Sucesso(emptyList())

        override suspend fun listar(status: StatusIdeia?, prioridade: Prioridade?) = lista

        override suspend fun aprovadas() = naoUsado<List<Ideia>>()

        override suspend fun avaliar(id: String, avaliacao: AvaliacaoIdeia) = naoUsado<Ideia>()

        override suspend fun obter(id: String) = naoUsado<Ideia>()

        override suspend fun criar(dados: DadosIdeia) = naoUsado<Ideia>()

        override suspend fun atualizar(id: String, dados: DadosIdeia) = naoUsado<Ideia>()

        override suspend fun remover(id: String) = naoUsado<Unit>()
    }

    private class FakeProjetos : ProjetoRepository {
        var lista: Resultado<List<Projeto>> = Resultado.Sucesso(emptyList())

        override suspend fun listar(status: StatusProjeto?, etapa: EtapaProjeto?, estrategiaId: String?) = lista

        override suspend fun obter(id: String) = naoUsado<Projeto>()

        override suspend fun criar(dados: DadosProjeto) = naoUsado<Projeto>()

        override suspend fun atualizar(id: String, dados: DadosProjeto) = naoUsado<Projeto>()

        override suspend fun atualizarAndamento(id: String, andamento: AndamentoProjeto) = naoUsado<Projeto>()

        override suspend fun adicionarResultado(id: String, dados: DadosResultado) = naoUsado<Projeto>()

        override suspend fun removerResultado(id: String, resultadoId: String) = naoUsado<Projeto>()

        override suspend fun remover(id: String) = naoUsado<Unit>()
    }

    private class FakeDashboard : DashboardRepository {
        var resumo: Resultado<ResumoDashboard> = Resultado.Falha(TipoErro.DESCONHECIDO, "não configurado no teste")

        override suspend fun resumo(estrategiaId: String?, projetoId: String?) = resumo

        override suspend fun porEstrategia() = naoUsado<List<DesempenhoEstrategia>>()

        override suspend fun porProjeto(estrategiaId: String?) = naoUsado<List<DesempenhoProjeto>>()

        override suspend fun evolucao(estrategiaId: String?, projetoId: String?) = naoUsado<List<EvolucaoMensal>>()
    }

    private companion object {
        fun <T> naoUsado(): Resultado<T> = Resultado.Falha(TipoErro.DESCONHECIDO, "não usado neste teste")
    }
}
