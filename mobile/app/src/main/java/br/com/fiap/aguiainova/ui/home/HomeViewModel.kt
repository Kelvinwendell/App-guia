package br.com.fiap.aguiainova.ui.home

import br.com.fiap.aguiainova.core.network.Resultado
import br.com.fiap.aguiainova.core.network.combinar
import br.com.fiap.aguiainova.core.network.mapear
import br.com.fiap.aguiainova.domain.model.Perfil
import br.com.fiap.aguiainova.domain.model.ResumoDashboard
import br.com.fiap.aguiainova.domain.model.StatusIdeia
import br.com.fiap.aguiainova.domain.repository.DashboardRepository
import br.com.fiap.aguiainova.domain.repository.IdeiaRepository
import br.com.fiap.aguiainova.domain.repository.ProjetoRepository
import br.com.fiap.aguiainova.ui.common.BaseViewModel
import br.com.fiap.aguiainova.ui.common.Estado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Numeros das telas iniciais; cada perfil preenche apenas o que exibe. */
data class ResumoHome(
    val ideiasTotal: Int = 0,
    val ideiasAprovadas: Int = 0,
    val ideiasPendentes: Int = 0,
    val projetosAtivos: Int = 0,
    val retornoParcial: Double = 0.0,
    val dashboard: ResumoDashboard? = null
)

class HomeViewModel(
    private val perfil: Perfil,
    private val ideias: IdeiaRepository,
    private val projetos: ProjetoRepository,
    private val dashboard: DashboardRepository
) : BaseViewModel() {

    private val _estado = MutableStateFlow<Estado<ResumoHome>>(Estado.Carregando)
    val estado: StateFlow<Estado<ResumoHome>> = _estado.asStateFlow()

    fun carregar() {
        _estado.carregar { buscar() }
    }

    private suspend fun buscar(): Resultado<ResumoHome> = when (perfil) {
        Perfil.OPERADOR -> ideias.listar().mapear { minhas ->
            ResumoHome(
                ideiasTotal = minhas.size,
                ideiasAprovadas = minhas.count { it.status == StatusIdeia.APROVADA }
            )
        }
        Perfil.GESTOR -> ideias.listar(status = StatusIdeia.EM_ANALISE).combinar(projetos.listar()) { pendentes, todos ->
            ResumoHome(
                ideiasPendentes = pendentes.size,
                projetosAtivos = todos.count { it.ativo },
                retornoParcial = todos.sumOf { it.retornoFinanceiro }
            )
        }
        Perfil.LIDERANCA -> dashboard.resumo().mapear { ResumoHome(dashboard = it) }
    }
}
