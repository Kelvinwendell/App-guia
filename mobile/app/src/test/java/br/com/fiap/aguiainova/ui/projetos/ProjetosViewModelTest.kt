package br.com.fiap.aguiainova.ui.projetos

import br.com.fiap.aguiainova.core.network.Resultado
import br.com.fiap.aguiainova.core.network.TipoErro
import br.com.fiap.aguiainova.domain.model.AndamentoProjeto
import br.com.fiap.aguiainova.domain.model.AvaliacaoIdeia
import br.com.fiap.aguiainova.domain.model.DadosEstrategia
import br.com.fiap.aguiainova.domain.model.DadosIdeia
import br.com.fiap.aguiainova.domain.model.DadosProjeto
import br.com.fiap.aguiainova.domain.model.DadosResultado
import br.com.fiap.aguiainova.domain.model.Estrategia
import br.com.fiap.aguiainova.domain.model.EstrategiaHistorico
import br.com.fiap.aguiainova.domain.model.EtapaProjeto
import br.com.fiap.aguiainova.domain.model.Ideia
import br.com.fiap.aguiainova.domain.model.Prioridade
import br.com.fiap.aguiainova.domain.model.Projeto
import br.com.fiap.aguiainova.domain.model.SaudeEstrategia
import br.com.fiap.aguiainova.domain.model.StatusEstrategia
import br.com.fiap.aguiainova.domain.model.StatusIdeia
import br.com.fiap.aguiainova.domain.model.StatusProjeto
import br.com.fiap.aguiainova.domain.repository.EstrategiaRepository
import br.com.fiap.aguiainova.domain.repository.IdeiaRepository
import br.com.fiap.aguiainova.domain.repository.ProjetoRepository
import br.com.fiap.aguiainova.ui.common.Estado
import br.com.fiap.aguiainova.ui.support.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * SPEC-RD-006: listagem de projetos, e formulario de criacao (vinculos opcionais com estrategia/ideia),
 * testados com repositorios falsos.
 */
class ProjetosViewModelTest {

    @get:Rule
    val principal = MainDispatcherRule()

    private val projetos = FakeProjetos()
    private val estrategias = FakeEstrategias()
    private val ideias = FakeIdeias()

    @Test
    fun `carregar mostra os projetos devolvidos pela API`() {
        projetos.lista = Resultado.Sucesso(listOf(projeto("1"), projeto("2")))

        val vm = ProjetosViewModel(projetos).also { it.carregar() }

        val estado = vm.estado.value as Estado.Pronto
        assertEquals(listOf("1", "2"), estado.dados.map { it.id })
    }

    @Test
    fun `falha ao carregar vira tela de erro com a mensagem`() {
        projetos.lista = Resultado.Falha(TipoErro.SERVIDOR, "Servidor indisponível.")

        val vm = ProjetosViewModel(projetos).also { it.carregar() }

        assertEquals(Estado.Erro("Servidor indisponível."), vm.estado.value)
    }

    @Test
    fun `formulario carrega estrategias vigentes e ideias aprovadas para vincular`() {
        estrategias.lista = Resultado.Sucesso(listOf(estrategia("e1")))
        ideias.aprovadas = Resultado.Sucesso(listOf(ideia("i1")))

        val vm = formViewModel().also { it.carregar() }

        val opcoes = (vm.estado.value as Estado.Pronto).dados
        assertEquals(listOf("e1"), opcoes.estrategiasVigentes.map { it.id })
        assertEquals(listOf("i1"), opcoes.ideiasAprovadas.map { it.id })
    }

    @Test
    fun `criar projeto sem vinculo de estrategia ou ideia nao e obrigatorio`() {
        projetos.criado = Resultado.Sucesso(projeto("novo"))
        val vm = formViewModel()
        var criado: Projeto? = null

        vm.criar(DadosProjeto("Nome", "Descrição", 1000.0, prazo = "2026-12-31"), { criado = it })

        assertEquals("novo", criado?.id)
        assertEquals(1, projetos.chamadasCriar)
    }

    @Test
    fun `criar projeto propaga o vinculo quando informado`() {
        projetos.criado = Resultado.Sucesso(projeto("novo"))
        val vm = formViewModel()

        vm.criar(DadosProjeto("Nome", "Descrição", 1000.0, prazo = "2026-12-31", estrategiaId = "e1", ideiaId = "i1"), {})

        assertEquals("e1", projetos.ultimoDados?.estrategiaId)
        assertEquals("i1", projetos.ultimoDados?.ideiaId)
    }

    @Test
    fun `falha ao criar avisa e nao chama o callback de sucesso`() {
        projetos.criado = Resultado.Falha(TipoErro.VALIDACAO, "Prazo inválido.")
        val vm = formViewModel()
        var chamado = false

        vm.criar(DadosProjeto("Nome", "Descrição", 1000.0, prazo = "2026-12-31"), { chamado = true })

        assertEquals("Prazo inválido.", vm.aviso.value)
        assertFalse(chamado)
    }

    @Test
    fun `avisarCampoInvalido expoe a mensagem de validacao do formulario`() {
        val vm = formViewModel()

        vm.avisarCampoInvalido("Informe um investimento válido.")

        assertEquals("Informe um investimento válido.", vm.aviso.value)
        assertTrue("nao chama a API so por avisar", projetos.chamadasCriar == 0)
    }

    // ---- apoio ----

    private fun formViewModel() = ProjetoFormViewModel(projetos, estrategias, ideias)

    private fun projeto(id: String) = Projeto(
        id = id,
        nome = "Projeto $id",
        descricao = "Descrição $id",
        status = StatusProjeto.EM_ANDAMENTO,
        etapa = EtapaProjeto.EXECUCAO,
        investimento = 1000.0,
        prazo = "2026-12-31",
        progresso = 10
    )

    private fun estrategia(id: String) = Estrategia(
        id = id, titulo = "Estratégia $id", descricao = "Descrição", categoria = "Tecnologia", status = StatusEstrategia.VIGENTE
    )

    private fun ideia(id: String) = Ideia(
        id = id,
        titulo = "Ideia $id",
        descricao = "Descrição",
        categoria = "Tecnologia",
        status = StatusIdeia.APROVADA,
        prioridade = Prioridade.MEDIA,
        autorId = "autor-1",
        autorNome = "Vinícius"
    )

    private class FakeProjetos : ProjetoRepository {
        var lista: Resultado<List<Projeto>> = Resultado.Sucesso(emptyList())
        var criado: Resultado<Projeto> = Resultado.Falha(TipoErro.DESCONHECIDO, "não configurado no teste")
        var chamadasCriar = 0
        var ultimoDados: DadosProjeto? = null

        override suspend fun listar(status: StatusProjeto?, etapa: EtapaProjeto?, estrategiaId: String?) = lista

        override suspend fun obter(id: String) = naoUsado<Projeto>()

        override suspend fun criar(dados: DadosProjeto): Resultado<Projeto> {
            chamadasCriar++
            ultimoDados = dados
            return criado
        }

        override suspend fun atualizar(id: String, dados: DadosProjeto) = naoUsado<Projeto>()

        override suspend fun atualizarAndamento(id: String, andamento: AndamentoProjeto) = naoUsado<Projeto>()

        override suspend fun adicionarResultado(id: String, dados: DadosResultado) = naoUsado<Projeto>()

        override suspend fun removerResultado(id: String, resultadoId: String) = naoUsado<Projeto>()

        override suspend fun remover(id: String) = naoUsado<Unit>()
    }

    private class FakeEstrategias : EstrategiaRepository {
        var lista: Resultado<List<Estrategia>> = Resultado.Sucesso(emptyList())

        override suspend fun listar(status: StatusEstrategia?) = lista

        override suspend fun obter(id: String) = naoUsado<Estrategia>()

        override suspend fun criar(dados: DadosEstrategia) = naoUsado<Estrategia>()

        override suspend fun atualizar(id: String, dados: DadosEstrategia) = naoUsado<Estrategia>()

        override suspend fun remover(id: String) = naoUsado<Unit>()

        override suspend fun historico(categoria: String?, campanha: String?) = naoUsado<List<EstrategiaHistorico>>()

        override suspend fun saude() = naoUsado<List<SaudeEstrategia>>()

        override suspend fun saudeDe(id: String) = naoUsado<SaudeEstrategia>()
    }

    private class FakeIdeias : IdeiaRepository {
        var aprovadas: Resultado<List<Ideia>> = Resultado.Sucesso(emptyList())

        override suspend fun listar(status: StatusIdeia?, prioridade: Prioridade?) = naoUsado<List<Ideia>>()

        override suspend fun aprovadas() = aprovadas

        override suspend fun avaliar(id: String, avaliacao: AvaliacaoIdeia) = naoUsado<Ideia>()

        override suspend fun obter(id: String) = naoUsado<Ideia>()

        override suspend fun criar(dados: DadosIdeia) = naoUsado<Ideia>()

        override suspend fun atualizar(id: String, dados: DadosIdeia) = naoUsado<Ideia>()

        override suspend fun remover(id: String) = naoUsado<Unit>()
    }

    private companion object {
        fun <T> naoUsado(): Resultado<T> = Resultado.Falha(TipoErro.DESCONHECIDO, "não usado neste teste")
    }
}
