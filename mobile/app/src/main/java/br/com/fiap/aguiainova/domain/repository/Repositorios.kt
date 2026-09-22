package br.com.fiap.aguiainova.domain.repository

import br.com.fiap.aguiainova.core.network.Resultado
import br.com.fiap.aguiainova.domain.model.AndamentoProjeto
import br.com.fiap.aguiainova.domain.model.AvaliacaoIdeia
import br.com.fiap.aguiainova.domain.model.AvaliacaoIdeiaIa
import br.com.fiap.aguiainova.domain.model.DadosEstrategia
import br.com.fiap.aguiainova.domain.model.DadosIdeia
import br.com.fiap.aguiainova.domain.model.DadosProjeto
import br.com.fiap.aguiainova.domain.model.DadosResultado
import br.com.fiap.aguiainova.domain.model.DesempenhoEstrategia
import br.com.fiap.aguiainova.domain.model.DesempenhoProjeto
import br.com.fiap.aguiainova.domain.model.Estrategia
import br.com.fiap.aguiainova.domain.model.EstrategiaHistorico
import br.com.fiap.aguiainova.domain.model.EtapaProjeto
import br.com.fiap.aguiainova.domain.model.EvolucaoMensal
import br.com.fiap.aguiainova.domain.model.Ideia
import br.com.fiap.aguiainova.domain.model.InsightsIa
import br.com.fiap.aguiainova.domain.model.MensagemChat
import br.com.fiap.aguiainova.domain.model.NovoUsuario
import br.com.fiap.aguiainova.domain.model.Prioridade
import br.com.fiap.aguiainova.domain.model.Projeto
import br.com.fiap.aguiainova.domain.model.RankingIa
import br.com.fiap.aguiainova.domain.model.RespostaChat
import br.com.fiap.aguiainova.domain.model.ResumoDashboard
import br.com.fiap.aguiainova.domain.model.StatusEstrategia
import br.com.fiap.aguiainova.domain.model.StatusIa
import br.com.fiap.aguiainova.domain.model.StatusIdeia
import br.com.fiap.aguiainova.domain.model.StatusProjeto
import br.com.fiap.aguiainova.domain.model.Usuario
import kotlinx.coroutines.flow.StateFlow

/*
 * Contratos de acesso aos dados. As telas (via ViewModel) dependem apenas destas interfaces: de onde os dados vem
 * (API REST, banco local, dados falsos nos testes) e um detalhe da implementacao.
 */

interface AuthRepository {
    /** Usuario logado; nulo quando nao ha sessao (ou ela expirou). */
    val usuario: StateFlow<Usuario?>

    suspend fun entrar(email: String, senha: String): Resultado<Usuario>

    suspend fun cadastrar(novo: NovoUsuario): Resultado<Usuario>

    fun sair()
}

interface EstrategiaRepository {
    suspend fun listar(status: StatusEstrategia? = null): Resultado<List<Estrategia>>

    suspend fun obter(id: String): Resultado<Estrategia>

    suspend fun criar(dados: DadosEstrategia): Resultado<Estrategia>

    suspend fun atualizar(id: String, dados: DadosEstrategia): Resultado<Estrategia>

    suspend fun remover(id: String): Resultado<Unit>

    suspend fun historico(categoria: String? = null, campanha: String? = null): Resultado<List<EstrategiaHistorico>>
}

interface IdeiaRepository {
    /** OPERADOR recebe so as proprias ideias; GESTOR recebe todas. */
    suspend fun listar(status: StatusIdeia? = null, prioridade: Prioridade? = null): Resultado<List<Ideia>>

    suspend fun aprovadas(): Resultado<List<Ideia>>

    suspend fun obter(id: String): Resultado<Ideia>

    suspend fun criar(dados: DadosIdeia): Resultado<Ideia>

    suspend fun atualizar(id: String, dados: DadosIdeia): Resultado<Ideia>

    suspend fun remover(id: String): Resultado<Unit>

    suspend fun avaliar(id: String, avaliacao: AvaliacaoIdeia): Resultado<Ideia>
}

interface ProjetoRepository {
    suspend fun listar(
        status: StatusProjeto? = null,
        etapa: EtapaProjeto? = null,
        estrategiaId: String? = null
    ): Resultado<List<Projeto>>

    suspend fun obter(id: String): Resultado<Projeto>

    suspend fun criar(dados: DadosProjeto): Resultado<Projeto>

    suspend fun atualizar(id: String, dados: DadosProjeto): Resultado<Projeto>

    suspend fun atualizarAndamento(id: String, andamento: AndamentoProjeto): Resultado<Projeto>

    suspend fun adicionarResultado(id: String, dados: DadosResultado): Resultado<Projeto>

    suspend fun removerResultado(id: String, resultadoId: String): Resultado<Projeto>

    suspend fun remover(id: String): Resultado<Unit>
}

interface DashboardRepository {
    suspend fun resumo(estrategiaId: String? = null, projetoId: String? = null): Resultado<ResumoDashboard>

    suspend fun porEstrategia(): Resultado<List<DesempenhoEstrategia>>

    suspend fun porProjeto(estrategiaId: String? = null): Resultado<List<DesempenhoProjeto>>

    suspend fun evolucao(estrategiaId: String? = null, projetoId: String? = null): Resultado<List<EvolucaoMensal>>
}

interface IaRepository {
    suspend fun status(): Resultado<StatusIa>

    suspend fun avaliarIdeia(ideiaId: String): Resultado<AvaliacaoIdeiaIa>

    suspend fun avaliarPendentes(): Resultado<RankingIa>

    suspend fun conversar(mensagem: String, historico: List<MensagemChat>): Resultado<RespostaChat>

    suspend fun insights(): Resultado<InsightsIa>
}
