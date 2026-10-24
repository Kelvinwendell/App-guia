package br.com.fiap.aguiainova.ui.estrategias

import br.com.fiap.aguiainova.core.network.Resultado
import br.com.fiap.aguiainova.core.network.TipoErro
import br.com.fiap.aguiainova.domain.model.AnaliseEstrategia
import br.com.fiap.aguiainova.domain.model.AvaliacaoIdeiaIa
import br.com.fiap.aguiainova.domain.model.DadosEstrategia
import br.com.fiap.aguiainova.domain.model.Estrategia
import br.com.fiap.aguiainova.domain.model.EstrategiaHistorico
import br.com.fiap.aguiainova.domain.model.InsightsIa
import br.com.fiap.aguiainova.domain.model.LeituraSaude
import br.com.fiap.aguiainova.domain.model.MensagemChat
import br.com.fiap.aguiainova.domain.model.NivelSaude
import br.com.fiap.aguiainova.domain.model.Perfil
import br.com.fiap.aguiainova.domain.model.RankingIa
import br.com.fiap.aguiainova.domain.model.RascunhoEstrategia
import br.com.fiap.aguiainova.domain.model.RespostaChat
import br.com.fiap.aguiainova.domain.model.SaudeEstrategia
import br.com.fiap.aguiainova.domain.model.StatusEstrategia
import br.com.fiap.aguiainova.domain.model.StatusIa
import br.com.fiap.aguiainova.domain.repository.EstrategiaRepository
import br.com.fiap.aguiainova.domain.repository.IaRepository
import br.com.fiap.aguiainova.ui.common.Estado
import br.com.fiap.aguiainova.ui.support.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Regras de apresentacao das estrategias (lista com saude, detalhe e formulario com IA), com repositorios falsos:
 * nada aqui depende de rede, de Retrofit ou da API.
 */
class EstrategiasViewModelsTest {

    @get:Rule
    val principal = MainDispatcherRule()

    private val estrategias = FakeEstrategias()
    private val ia = FakeIa()

    // ---------------- lista ----------------

    @Test
    fun `operador ve a lista sem que a saude seja sequer pedida`() {
        val vm = EstrategiasViewModel(Perfil.OPERADOR, estrategias).also { it.carregar() }

        val painel = (vm.estado.value as Estado.Pronto).dados
        assertEquals(listOf("a", "b"), painel.estrategias.map { it.id })
        assertTrue(painel.saude.isEmpty())
        assertEquals(0, estrategias.pedidosDeSaude)
    }

    @Test
    fun `gestor recebe a saude indexada pelo id da estrategia`() {
        val vm = EstrategiasViewModel(Perfil.GESTOR, estrategias).also { it.carregar() }

        val painel = (vm.estado.value as Estado.Pronto).dados
        assertEquals(NivelSaude.EM_RISCO, painel.saude.getValue("a").nivel)
        assertEquals(setOf("a"), painel.saude.keys)
    }

    @Test
    fun `falha ao calcular a saude nao derruba a lista`() {
        estrategias.saude = falha("Sem permissão.")

        val vm = EstrategiasViewModel(Perfil.LIDERANCA, estrategias).also { it.carregar() }

        val painel = (vm.estado.value as Estado.Pronto).dados
        assertEquals(2, painel.estrategias.size)
        assertTrue(painel.saude.isEmpty())
    }

    @Test
    fun `falha ao listar vira tela de erro`() {
        estrategias.lista = falha("Sem conexão com o servidor.")

        val vm = EstrategiasViewModel(Perfil.GESTOR, estrategias).also { it.carregar() }

        assertEquals(Estado.Erro("Sem conexão com o servidor."), vm.estado.value)
    }

    @Test
    fun `painel separa vigentes de encerradas e conta so as vigentes por nivel de saude`() {
        val painel = PainelEstrategias(
            listOf(criarEstrategia("a"), criarEstrategia("b", StatusEstrategia.ENCERRADA), criarEstrategia("c")),
            mapOf(
                "a" to criarSaude("a", NivelSaude.EM_RISCO),
                "b" to criarSaude("b", NivelSaude.EM_DIA),
                "c" to criarSaude("c", NivelSaude.EM_RISCO)
            )
        )

        assertEquals(listOf("a", "c"), painel.vigentes.map { it.id })
        assertEquals(listOf("b"), painel.encerradas.map { it.id })
        assertEquals(mapOf(NivelSaude.EM_RISCO to 2), painel.contagemPorNivel())
    }

    // ---------------- detalhe ----------------

    private fun detalhe(id: String, perfil: Perfil) = EstrategiaDetalheViewModel(id, perfil, estrategias, ia)

    @Test
    fun `detalhe de estrategia vigente traz a saude para a lideranca`() {
        val vm = detalhe("a", Perfil.LIDERANCA).also { it.carregar() }

        val dados = (vm.estado.value as Estado.Pronto).dados
        assertEquals("a", dados.estrategia.id)
        assertEquals(NivelSaude.EM_RISCO, dados.saude?.nivel)
    }

    @Test
    fun `operador e estrategia encerrada nao pedem a saude`() {
        val operador = detalhe("a", Perfil.OPERADOR).also { it.carregar() }
        val encerrada = detalhe("b", Perfil.GESTOR).also { it.carregar() }

        assertNull((operador.estado.value as Estado.Pronto).dados.saude)
        assertNull((encerrada.estado.value as Estado.Pronto).dados.saude)
        assertEquals(0, estrategias.pedidosDeSaudePorId)
    }

    @Test
    fun `leitura da IA so e pedida quando a pessoa pede`() {
        val vm = detalhe("a", Perfil.GESTOR).also { it.carregar() }
        assertNull(vm.leitura.value)

        ia.leitura = Resultado.Sucesso(LeituraSaude(criarSaude("a", NivelSaude.EM_RISCO), "Atraso.", listOf("Renegociar"), "GEMINI"))
        vm.lerComIa()

        assertEquals("Atraso.", (vm.leitura.value as Estado.Pronto).dados.leitura)
    }

    @Test
    fun `falha na leitura da IA mostra a mensagem sem perder o detalhe`() {
        val vm = detalhe("a", Perfil.GESTOR).also { it.carregar() }
        ia.leitura = falha("A IA está indisponível no momento.")

        vm.lerComIa()

        assertEquals(Estado.Erro("A IA está indisponível no momento."), vm.leitura.value)
        assertTrue(vm.estado.value is Estado.Pronto)
    }

    @Test
    fun `excluir volta a tela so quando o servidor aceita`() {
        val vm = detalhe("a", Perfil.LIDERANCA)
        var voltou = false

        estrategias.remocao = falha("A estratégia possui ideias ou projetos vinculados.")
        vm.excluir { voltou = true }
        assertTrue(!voltou)
        assertEquals("A estratégia possui ideias ou projetos vinculados.", vm.aviso.value)

        estrategias.remocao = Resultado.Sucesso(Unit)
        vm.excluir { voltou = true }
        assertTrue(voltou)
    }

    // ---------------- formulario ----------------

    private fun formulario(id: String? = null) = EstrategiaFormViewModel(id, estrategias, ia)

    private fun dados(titulo: String = "T", descricao: String = "D", categoria: String = "C") =
        DadosEstrategia(titulo, descricao, categoria)

    @Test
    fun `rascunho sem objetivo avisa e nao chama a IA`() {
        val vm = formulario()

        vm.rascunhar("   ") { error("nao deveria preencher") }

        assertEquals("Descreva o objetivo para a IA escrever o rascunho.", vm.aviso.value)
        assertEquals(0, ia.pedidosDeRascunho)
    }

    @Test
    fun `rascunho gerado preenche o formulario e pede revisao`() {
        ia.rascunho = Resultado.Sucesso(
            RascunhoEstrategia("Título IA", "Descrição IA", "Tecnologia", null, listOf("Meta 1"), "2026-11-01", "2027-04-30", "GEMINI")
        )
        val vm = formulario()
        var recebido: RascunhoEstrategia? = null

        vm.rascunhar("  Reduzir paradas  ") { recebido = it }

        assertEquals("Título IA", recebido?.titulo)
        assertEquals("Reduzir paradas", ia.ultimoObjetivo)
        assertEquals("Rascunho gerado por IA. Revise antes de salvar.", vm.aviso.value)
    }

    @Test
    fun `sem IA o rascunho avisa o motivo e nao preenche nada`() {
        ia.rascunho = Resultado.Falha(TipoErro.INDISPONIVEL, "A IA não está configurada neste ambiente.")
        val vm = formulario()

        vm.rascunhar("Crescer") { error("nao deveria preencher") }

        assertEquals("A IA não está configurada neste ambiente.", vm.aviso.value)
    }

    @Test
    fun `analise exige titulo descricao e categoria`() {
        val vm = formulario()

        vm.analisar(dados(titulo = " "))

        assertEquals("Preencha título, descrição e categoria para analisar.", vm.aviso.value)
        assertNull(vm.analise.value)
        assertEquals(0, ia.pedidosDeAnalise)
    }

    @Test
    fun `analise de estrategia ja salva envia o id para nao compara-la consigo mesma`() {
        ia.analise = Resultado.Sucesso(AnaliseEstrategia(72, "Boa base", fonte = "HEURISTICA"))
        val vm = formulario(id = "a")

        vm.analisar(dados())

        assertEquals(72, (vm.analise.value as Estado.Pronto).dados.nota)
        assertEquals("a", ia.ultimoIdAnalisado)
    }

    @Test
    fun `salvar valida os campos obrigatorios e escolhe entre criar e atualizar`() {
        val novo = formulario().also { it.salvar(dados(categoria = ""), {}) }
        assertEquals("Preencha título, descrição e categoria.", novo.aviso.value)

        var salvou = 0
        formulario().salvar(dados()) { salvou++ }
        formulario(id = "a").salvar(dados()) { salvou++ }

        assertEquals(2, salvou)
        assertEquals(1, estrategias.criadas)
        assertEquals(1, estrategias.atualizadas)
    }

    // ---------------- historico ----------------

    @Test
    fun `historico carrega todos os registros sem filtro`() {
        val vm = HistoricoEstrategiasViewModel(estrategias).also { it.carregar() }

        assertEquals(1, (vm.estado.value as Estado.Pronto).dados.size)
    }

    // ---------------- apoio ----------------

    private fun falha(mensagem: String) = Resultado.Falha(TipoErro.DESCONHECIDO, mensagem)

    private companion object {
        fun criarEstrategia(id: String, status: StatusEstrategia = StatusEstrategia.VIGENTE) =
            Estrategia(id, "Estratégia $id", "Descrição", "Geral", status = status)

        fun criarSaude(id: String, nivel: NivelSaude) = SaudeEstrategia(
            estrategiaId = id, titulo = "Estratégia $id", nivel = nivel, motivos = listOf("motivo"),
            proximaAcao = "agir", ideias = 1, ideiasEmAnalise = 0, aprovadasSemProjeto = 0, projetos = 1,
            projetosAtrasados = 0, investimento = 0.0, retorno = 0.0
        )
    }

    private class FakeEstrategias : EstrategiaRepository {
        var lista: Resultado<List<Estrategia>> =
            Resultado.Sucesso(listOf(criarEstrategia("a"), criarEstrategia("b", StatusEstrategia.ENCERRADA)))
        var saude: Resultado<List<SaudeEstrategia>> = Resultado.Sucesso(listOf(criarSaude("a", NivelSaude.EM_RISCO)))
        var remocao: Resultado<Unit> = Resultado.Sucesso(Unit)
        var pedidosDeSaude = 0
        var pedidosDeSaudePorId = 0
        var criadas = 0
        var atualizadas = 0

        override suspend fun listar(status: StatusEstrategia?) = lista

        override suspend fun obter(id: String): Resultado<Estrategia> =
            Resultado.Sucesso(criarEstrategia(id, if (id == "b") StatusEstrategia.ENCERRADA else StatusEstrategia.VIGENTE))

        override suspend fun criar(dados: DadosEstrategia): Resultado<Estrategia> {
            criadas++
            return Resultado.Sucesso(criarEstrategia("nova"))
        }

        override suspend fun atualizar(id: String, dados: DadosEstrategia): Resultado<Estrategia> {
            atualizadas++
            return Resultado.Sucesso(criarEstrategia(id))
        }

        override suspend fun remover(id: String) = remocao

        override suspend fun historico(categoria: String?, campanha: String?): Resultado<List<EstrategiaHistorico>> =
            Resultado.Sucesso(
                listOf(
                    EstrategiaHistorico(
                        "h1", "a", "2026-01-01T10:00:00Z", "Geral", null,
                        br.com.fiap.aguiainova.domain.model.AcaoEstrategia.CRIADA, "Estratégia a", "Descrição",
                        StatusEstrategia.VIGENTE
                    )
                )
            )

        override suspend fun saude(): Resultado<List<SaudeEstrategia>> {
            pedidosDeSaude++
            return saude
        }

        override suspend fun saudeDe(id: String): Resultado<SaudeEstrategia> {
            pedidosDeSaudePorId++
            return Resultado.Sucesso(criarSaude(id, NivelSaude.EM_RISCO))
        }
    }

    private class FakeIa : IaRepository {
        var rascunho: Resultado<RascunhoEstrategia> = Resultado.Falha(TipoErro.DESCONHECIDO, "não configurado no teste")
        var analise: Resultado<AnaliseEstrategia> = Resultado.Falha(TipoErro.DESCONHECIDO, "não configurado no teste")
        var leitura: Resultado<LeituraSaude> = Resultado.Falha(TipoErro.DESCONHECIDO, "não configurado no teste")
        var pedidosDeRascunho = 0
        var pedidosDeAnalise = 0
        var ultimoObjetivo: String? = null
        var ultimoIdAnalisado: String? = null

        override suspend fun rascunharEstrategia(objetivo: String): Resultado<RascunhoEstrategia> {
            pedidosDeRascunho++
            ultimoObjetivo = objetivo
            return rascunho
        }

        override suspend fun analisarEstrategia(estrategiaId: String?, dados: DadosEstrategia): Resultado<AnaliseEstrategia> {
            pedidosDeAnalise++
            ultimoIdAnalisado = estrategiaId
            return analise
        }

        override suspend fun lerSaude(estrategiaId: String) = leitura

        override suspend fun status() = naoUsado<StatusIa>()

        override suspend fun avaliarIdeia(ideiaId: String) = naoUsado<AvaliacaoIdeiaIa>()

        override suspend fun avaliarPendentes() = naoUsado<RankingIa>()

        override suspend fun conversar(mensagem: String, historico: List<MensagemChat>) = naoUsado<RespostaChat>()

        override suspend fun insights() = naoUsado<InsightsIa>()

        private fun <T> naoUsado(): Resultado<T> = Resultado.Falha(TipoErro.DESCONHECIDO, "não usado neste teste")
    }
}
