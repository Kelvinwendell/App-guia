package br.com.fiap.aguiainova.ui.estrategias

import br.com.fiap.aguiainova.core.network.Resultado
import br.com.fiap.aguiainova.core.network.TipoErro
import br.com.fiap.aguiainova.domain.model.AcaoEstrategia
import br.com.fiap.aguiainova.domain.model.DadosEstrategia
import br.com.fiap.aguiainova.domain.model.Estrategia
import br.com.fiap.aguiainova.domain.model.EstrategiaHistorico
import br.com.fiap.aguiainova.domain.model.SaudeEstrategia
import br.com.fiap.aguiainova.domain.model.StatusEstrategia
import br.com.fiap.aguiainova.domain.repository.EstrategiaRepository
import br.com.fiap.aguiainova.ui.common.Estado
import br.com.fiap.aguiainova.ui.support.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * SPEC-RD-007: o filtro do historico vai para o repositorio (a API filtra), "tentar novamente" repete a mesma busca e
 * as opcoes dos seletores nao encolhem quando um filtro e aplicado.
 */
class HistoricoEstrategiasViewModelTest {

    @get:Rule
    val principal = MainDispatcherRule()

    private val estrategias = FakeHistorico()

    @Test
    fun `primeira carga pede o historico sem filtro e monta as opcoes`() {
        val vm = HistoricoEstrategiasViewModel(estrategias).also { it.carregar() }

        assertEquals(listOf(FiltroHistorico()), estrategias.pedidos)
        assertEquals(listOf("Custos", "Geral"), vm.opcoes.value.categorias)
        assertEquals(listOf("Verão"), vm.opcoes.value.campanhas)
    }

    @Test
    fun `filtrar envia categoria e campanha ao repositorio`() {
        val vm = HistoricoEstrategiasViewModel(estrategias).also { it.carregar() }

        vm.filtrarPor("Custos", "Verão")

        assertEquals(FiltroHistorico("Custos", "Verão"), estrategias.pedidos.last())
        assertEquals(listOf("h2"), (vm.estado.value as Estado.Pronto).dados.map { it.id })
    }

    @Test
    fun `opcoes continuam completas depois de filtrar`() {
        val vm = HistoricoEstrategiasViewModel(estrategias).also { it.carregar() }

        vm.filtrarPor("Custos", null)

        assertEquals(listOf("Custos", "Geral"), vm.opcoes.value.categorias)
    }

    @Test
    fun `tentar novamente repete o filtro escolhido`() {
        val vm = HistoricoEstrategiasViewModel(estrategias).also { it.carregar() }
        estrategias.falhar = true
        vm.filtrarPor("Geral", null)

        estrategias.falhar = false
        vm.carregar()

        assertEquals(FiltroHistorico("Geral", null), estrategias.pedidos.last())
        assertEquals(FiltroHistorico("Geral", null), vm.filtro.value)
    }

    @Test
    fun `falha na primeira carga vira tela de erro`() {
        estrategias.falhar = true

        val vm = HistoricoEstrategiasViewModel(estrategias).also { it.carregar() }

        assertTrue(vm.estado.value is Estado.Erro)
    }

    @Test
    fun `escolher o mesmo filtro nao busca de novo`() {
        val vm = HistoricoEstrategiasViewModel(estrategias).also { it.carregar() }

        vm.filtrarPor(null, null)

        assertEquals(1, estrategias.pedidos.size)
    }

    private class FakeHistorico : EstrategiaRepository {
        val pedidos = mutableListOf<FiltroHistorico>()
        var falhar = false

        private val registros = listOf(
            registro("h1", "Geral", null),
            registro("h2", "Custos", "Verão"),
            registro("h3", "Custos", null)
        )

        override suspend fun historico(categoria: String?, campanha: String?): Resultado<List<EstrategiaHistorico>> {
            pedidos += FiltroHistorico(categoria, campanha)
            if (falhar) return Resultado.Falha(TipoErro.REDE, "Sem conexão com o servidor.")
            return Resultado.Sucesso(
                registros.filter { (categoria == null || it.categoria == categoria) && (campanha == null || it.campanha == campanha) }
            )
        }

        override suspend fun listar(status: StatusEstrategia?) = naoUsado<List<Estrategia>>()

        override suspend fun obter(id: String) = naoUsado<Estrategia>()

        override suspend fun criar(dados: DadosEstrategia) = naoUsado<Estrategia>()

        override suspend fun atualizar(id: String, dados: DadosEstrategia) = naoUsado<Estrategia>()

        override suspend fun remover(id: String) = naoUsado<Unit>()

        override suspend fun saude() = naoUsado<List<SaudeEstrategia>>()

        override suspend fun saudeDe(id: String) = naoUsado<SaudeEstrategia>()

        private fun <T> naoUsado(): Resultado<T> = Resultado.Falha(TipoErro.DESCONHECIDO, "não usado neste teste")

        private fun registro(id: String, categoria: String, campanha: String?) = EstrategiaHistorico(
            id, "e-$id", "2026-01-01T10:00:00Z", categoria, campanha, AcaoEstrategia.CRIADA, "Estratégia $id", "Descrição",
            StatusEstrategia.VIGENTE
        )
    }
}
