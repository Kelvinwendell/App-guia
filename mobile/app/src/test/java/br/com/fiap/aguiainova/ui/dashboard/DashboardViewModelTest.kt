package br.com.fiap.aguiainova.ui.dashboard

import br.com.fiap.aguiainova.core.network.Resultado
import br.com.fiap.aguiainova.core.network.TipoErro
import br.com.fiap.aguiainova.domain.model.AnaliseEstrategia
import br.com.fiap.aguiainova.domain.model.AvaliacaoIdeiaIa
import br.com.fiap.aguiainova.domain.model.DadosEstrategia
import br.com.fiap.aguiainova.domain.model.DesempenhoEstrategia
import br.com.fiap.aguiainova.domain.model.DesempenhoProjeto
import br.com.fiap.aguiainova.domain.model.Estrategia
import br.com.fiap.aguiainova.domain.model.EstrategiaHistorico
import br.com.fiap.aguiainova.domain.model.EvolucaoMensal
import br.com.fiap.aguiainova.domain.model.InsightsIa
import br.com.fiap.aguiainova.domain.model.LeituraSaude
import br.com.fiap.aguiainova.domain.model.MensagemChat
import br.com.fiap.aguiainova.domain.model.RankingIa
import br.com.fiap.aguiainova.domain.model.RascunhoEstrategia
import br.com.fiap.aguiainova.domain.model.RespostaChat
import br.com.fiap.aguiainova.domain.model.ResumoDashboard
import br.com.fiap.aguiainova.domain.model.ResumoIdeias
import br.com.fiap.aguiainova.domain.model.SaudeEstrategia
import br.com.fiap.aguiainova.domain.model.StatusEstrategia
import br.com.fiap.aguiainova.domain.model.StatusIa
import br.com.fiap.aguiainova.domain.repository.DashboardRepository
import br.com.fiap.aguiainova.domain.repository.EstrategiaRepository
import br.com.fiap.aguiainova.domain.repository.IaRepository
import br.com.fiap.aguiainova.ui.common.Estado
import br.com.fiap.aguiainova.ui.support.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * SPEC-RD-008: o filtro chega a resumo e evolucao, o comparativo por estrategia continua global, uma falha ao
 * atualizar nao apaga as metricas que ja estavam na tela e abrir o painel nao gasta cota da IA.
 */
class DashboardViewModelTest {

    @get:Rule
    val principal = MainDispatcherRule()

    private val dashboard = FakeDashboard()
    private val ia = FakeIa()
    private fun criarVm() = DashboardViewModel(dashboard, FakeEstrategias(), ia)

    @Test
    fun `filtro encaminha a estrategia para resumo e evolucao`() {
        val vm = criarVm().also { it.carregar() }

        vm.filtrarPorEstrategia("e1")

        assertEquals("e1", dashboard.resumoPedidoCom)
        assertEquals("e1", dashboard.evolucaoPedidaCom)
        assertEquals("e1", vm.estrategiaFiltro.value)
    }

    @Test
    fun `comparativo por estrategia continua global com filtro`() {
        val vm = criarVm().also { it.carregar() }

        vm.filtrarPorEstrategia("e1")

        assertEquals(2, dashboard.pedidosPorEstrategia)
        assertEquals(2, (vm.estado.value as Estado.Pronto).dados.porEstrategia.size)
    }

    @Test
    fun `falha ao atualizar mantem as metricas anteriores`() {
        val vm = criarVm().also { it.carregar() }
        dashboard.resumo = Resultado.Falha(TipoErro.REDE, "Sem conexão com o servidor.")

        vm.filtrarPorEstrategia("e1")

        val dados = (vm.estado.value as Estado.Pronto).dados
        assertEquals(12.5, dados.resumo.roiPercentual)
        assertEquals("Sem conexão com o servidor.", vm.aviso.value)
    }

    @Test
    fun `falha na primeira carga vira tela de erro`() {
        dashboard.resumo = Resultado.Falha(TipoErro.SERVIDOR, "Erro no servidor.")

        val vm = criarVm().also { it.carregar() }

        assertTrue(vm.estado.value is Estado.Erro)
    }

    @Test
    fun `abrir e atualizar o painel nao chama a IA`() {
        val vm = criarVm().also { it.carregar() }
        vm.carregar()

        assertEquals(0, ia.pedidosDeInsights)
        assertNull(vm.insights.value)
    }

    @Test
    fun `insights so sao pedidos quando a lideranca aciona`() {
        val vm = criarVm().also { it.carregar() }

        vm.gerarInsights()

        assertEquals(1, ia.pedidosDeInsights)
    }

    @Test
    fun `segundo pedido de insights durante a geracao e ignorado`() {
        ia.insightsSuspenso = CompletableDeferred()
        val vm = criarVm().also { it.carregar() }

        vm.gerarInsights()
        vm.gerarInsights()

        assertEquals(1, ia.pedidosDeInsights)
        ia.insightsSuspenso?.complete(Resultado.Sucesso(InsightsIa("ok", fonte = "GEMINI")))
        assertTrue(vm.insights.value is Estado.Pronto)
    }

    private class FakeDashboard : DashboardRepository {
        var resumo: Resultado<ResumoDashboard> = Resultado.Sucesso(
            ResumoDashboard(
                investimentoTotal = 1000.0, retornoTotal = 1125.0, lucroTotal = 125.0, roiPercentual = 12.5,
                totalProjetos = 2, projetosAtrasados = 0, ideias = ResumoIdeias(3, 1, 1, 1)
            )
        )
        var resumoPedidoCom: String? = "nunca pedido"
        var evolucaoPedidaCom: String? = "nunca pedido"
        var pedidosPorEstrategia = 0

        override suspend fun resumo(estrategiaId: String?, projetoId: String?): Resultado<ResumoDashboard> {
            resumoPedidoCom = estrategiaId
            return resumo
        }

        override suspend fun porEstrategia(): Resultado<List<DesempenhoEstrategia>> {
            pedidosPorEstrategia++
            return Resultado.Sucesso(
                listOf(
                    DesempenhoEstrategia("e1", "Energia", projetos = 1, ideias = 2, investimento = 1.0, retorno = 2.0, lucro = 1.0),
                    DesempenhoEstrategia(null, "Sem estratégia", projetos = 1, ideias = 1, investimento = 1.0, retorno = 0.0, lucro = -1.0)
                )
            )
        }

        override suspend fun evolucao(estrategiaId: String?, projetoId: String?): Resultado<List<EvolucaoMensal>> {
            evolucaoPedidaCom = estrategiaId
            return Resultado.Sucesso(listOf(EvolucaoMensal("2026-01", 100.0, 100.0)))
        }

        override suspend fun porProjeto(estrategiaId: String?) = naoUsado<List<DesempenhoProjeto>>()
    }

    private class FakeEstrategias : EstrategiaRepository {
        override suspend fun saude(): Resultado<List<SaudeEstrategia>> = Resultado.Sucesso(emptyList())

        override suspend fun listar(status: StatusEstrategia?) = naoUsado<List<Estrategia>>()

        override suspend fun obter(id: String) = naoUsado<Estrategia>()

        override suspend fun criar(dados: DadosEstrategia) = naoUsado<Estrategia>()

        override suspend fun atualizar(id: String, dados: DadosEstrategia) = naoUsado<Estrategia>()

        override suspend fun remover(id: String) = naoUsado<Unit>()

        override suspend fun historico(categoria: String?, campanha: String?) = naoUsado<List<EstrategiaHistorico>>()

        override suspend fun saudeDe(id: String) = naoUsado<SaudeEstrategia>()
    }

    private class FakeIa : IaRepository {
        var pedidosDeInsights = 0
        var insightsSuspenso: CompletableDeferred<Resultado<InsightsIa>>? = null

        override suspend fun insights(): Resultado<InsightsIa> {
            pedidosDeInsights++
            return insightsSuspenso?.await() ?: naoUsado()
        }

        override suspend fun status() = naoUsado<StatusIa>()

        override suspend fun avaliarIdeia(ideiaId: String) = naoUsado<AvaliacaoIdeiaIa>()

        override suspend fun avaliarPendentes() = naoUsado<RankingIa>()

        override suspend fun conversar(mensagem: String, historico: List<MensagemChat>) = naoUsado<RespostaChat>()

        override suspend fun rascunharEstrategia(objetivo: String) = naoUsado<RascunhoEstrategia>()

        override suspend fun analisarEstrategia(estrategiaId: String?, dados: DadosEstrategia) = naoUsado<AnaliseEstrategia>()

        override suspend fun lerSaude(estrategiaId: String) = naoUsado<LeituraSaude>()
    }

    private companion object {
        fun <T> naoUsado(): Resultado<T> = Resultado.Falha(TipoErro.DESCONHECIDO, "não usado neste teste")
    }
}
