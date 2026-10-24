package br.com.fiap.aguiainova.ui.projetos

import br.com.fiap.aguiainova.core.network.Resultado
import br.com.fiap.aguiainova.core.network.TipoErro
import br.com.fiap.aguiainova.domain.model.AndamentoProjeto
import br.com.fiap.aguiainova.domain.model.DadosProjeto
import br.com.fiap.aguiainova.domain.model.DadosResultado
import br.com.fiap.aguiainova.domain.model.EtapaProjeto
import br.com.fiap.aguiainova.domain.model.Projeto
import br.com.fiap.aguiainova.domain.model.StatusProjeto
import br.com.fiap.aguiainova.domain.repository.ProjetoRepository
import br.com.fiap.aguiainova.ui.common.Estado
import br.com.fiap.aguiainova.ui.support.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * SPEC-RD-006: andamento/resultados/exclusao de um projeto, testados com um repositorio falso. O gestor
 * atualiza; a tela so decide quem ve os controles (perfil), o VM nao sabe de perfil.
 */
class ProjetoDetalheViewModelTest {

    @get:Rule
    val principal = MainDispatcherRule()

    private val projetos = FakeProjetos()

    private fun viewModel(id: String = "1") = ProjetoDetalheViewModel(id, projetos)

    @Test
    fun `carregar mostra o projeto devolvido pela API`() {
        projetos.resultadoObter = Resultado.Sucesso(projeto(progresso = 40))

        val vm = viewModel().also { it.carregar() }

        assertEquals(40, (vm.estado.value as Estado.Pronto).dados.progresso)
    }

    @Test
    fun `salvar andamento atualiza o estado com o projeto devolvido e avisa`() {
        projetos.resultadoObter = Resultado.Sucesso(projeto(progresso = 10))
        val vm = viewModel().also { it.carregar() }
        projetos.andamento = Resultado.Sucesso(projeto(progresso = 60))

        vm.salvarAndamento(AndamentoProjeto(progresso = 60))

        assertEquals(60, (vm.estado.value as Estado.Pronto).dados.progresso)
        assertEquals("Andamento atualizado.", vm.aviso.value)
    }

    @Test
    fun `falha ao salvar andamento avisa e mantem o estado anterior`() {
        projetos.resultadoObter = Resultado.Sucesso(projeto(progresso = 10))
        val vm = viewModel().also { it.carregar() }
        projetos.andamento = Resultado.Falha(TipoErro.VALIDACAO, "Progresso inválido.")

        vm.salvarAndamento(AndamentoProjeto(progresso = 60))

        assertEquals("Progresso inválido.", vm.aviso.value)
        assertEquals(10, (vm.estado.value as Estado.Pronto).dados.progresso)
    }

    @Test
    fun `adicionar resultado atualiza o estado e avisa`() {
        projetos.resultado = Resultado.Sucesso(projeto(progresso = 10))
        val vm = viewModel()

        vm.adicionarResultado(DadosResultado(descricao = "Economia de energia", retornoFinanceiro = 500.0))

        assertEquals("Resultado registrado.", vm.aviso.value)
        assertEquals(1, projetos.chamadasAdicionarResultado)
    }

    @Test
    fun `remover resultado atualiza o estado e avisa`() {
        projetos.semResultado = Resultado.Sucesso(projeto(progresso = 10))
        val vm = viewModel()

        vm.removerResultado("resultado-1")

        assertEquals("Resultado removido.", vm.aviso.value)
        assertEquals("resultado-1", projetos.ultimoResultadoRemovidoId)
    }

    @Test
    fun `excluir com sucesso chama o callback`() {
        projetos.remocao = Resultado.Sucesso(Unit)
        val vm = viewModel()
        var chamado = false

        vm.excluir { chamado = true }

        assertTrue(chamado)
    }

    @Test
    fun `falha ao excluir avisa e nao chama o callback`() {
        projetos.remocao = Resultado.Falha(TipoErro.PROIBIDO, "Você não tem permissão para esta ação.")
        val vm = viewModel()
        var chamado = false

        vm.excluir { chamado = true }

        assertEquals("Você não tem permissão para esta ação.", vm.aviso.value)
        assertFalse(chamado)
    }

    @Test
    fun `avisarCampoInvalido expoe a mensagem sem chamar a API`() {
        val vm = viewModel()

        vm.avisarCampoInvalido("Informe um retorno financeiro válido.")

        assertEquals("Informe um retorno financeiro válido.", vm.aviso.value)
        assertEquals(0, projetos.chamadasAdicionarResultado)
    }

    // ---- apoio ----

    private fun projeto(progresso: Int) = Projeto(
        id = "1",
        nome = "Projeto",
        descricao = "Descrição",
        status = StatusProjeto.EM_ANDAMENTO,
        etapa = EtapaProjeto.EXECUCAO,
        investimento = 1000.0,
        prazo = "2026-12-31",
        progresso = progresso
    )

    private class FakeProjetos : ProjetoRepository {
        var resultadoObter: Resultado<Projeto> = Resultado.Falha(TipoErro.DESCONHECIDO, "não configurado no teste")
        var andamento: Resultado<Projeto> = Resultado.Falha(TipoErro.DESCONHECIDO, "não configurado no teste")
        var resultado: Resultado<Projeto> = Resultado.Falha(TipoErro.DESCONHECIDO, "não configurado no teste")
        var semResultado: Resultado<Projeto> = Resultado.Falha(TipoErro.DESCONHECIDO, "não configurado no teste")
        var remocao: Resultado<Unit> = Resultado.Falha(TipoErro.DESCONHECIDO, "não configurado no teste")
        var chamadasAdicionarResultado = 0
        var ultimoResultadoRemovidoId: String? = null

        override suspend fun listar(status: StatusProjeto?, etapa: EtapaProjeto?, estrategiaId: String?) = naoUsado<List<Projeto>>()

        override suspend fun obter(id: String) = resultadoObter

        override suspend fun criar(dados: DadosProjeto) = naoUsado<Projeto>()

        override suspend fun atualizar(id: String, dados: DadosProjeto) = naoUsado<Projeto>()

        override suspend fun atualizarAndamento(id: String, andamento: AndamentoProjeto) = this.andamento

        override suspend fun adicionarResultado(id: String, dados: DadosResultado): Resultado<Projeto> {
            chamadasAdicionarResultado++
            return resultado
        }

        override suspend fun removerResultado(id: String, resultadoId: String): Resultado<Projeto> {
            ultimoResultadoRemovidoId = resultadoId
            return semResultado
        }

        override suspend fun remover(id: String) = remocao
    }

    private companion object {
        fun <T> naoUsado(): Resultado<T> = Resultado.Falha(TipoErro.DESCONHECIDO, "não usado neste teste")
    }
}
