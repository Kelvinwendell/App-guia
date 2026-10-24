package br.com.fiap.aguiainova.ui.dashboard

import androidx.lifecycle.viewModelScope
import br.com.fiap.aguiainova.core.network.combinar
import br.com.fiap.aguiainova.core.network.dadosOuNulo
import br.com.fiap.aguiainova.core.network.mapear
import br.com.fiap.aguiainova.domain.model.DesempenhoEstrategia
import br.com.fiap.aguiainova.domain.model.EvolucaoMensal
import br.com.fiap.aguiainova.domain.model.InsightsIa
import br.com.fiap.aguiainova.domain.model.ResumoDashboard
import br.com.fiap.aguiainova.domain.model.SaudeEstrategia
import br.com.fiap.aguiainova.domain.repository.DashboardRepository
import br.com.fiap.aguiainova.domain.repository.EstrategiaRepository
import br.com.fiap.aguiainova.domain.repository.IaRepository
import br.com.fiap.aguiainova.ui.common.BaseViewModel
import br.com.fiap.aguiainova.ui.common.Estado
import br.com.fiap.aguiainova.ui.common.paraEstado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DadosDashboard(
    val resumo: ResumoDashboard,
    val porEstrategia: List<DesempenhoEstrategia>,
    val evolucao: List<EvolucaoMensal>,
    /** Saude de cada estrategia vigente; vazia se nao foi possivel calcula-la (o resto do painel segue valendo). */
    val saude: List<SaudeEstrategia> = emptyList()
)

class DashboardViewModel(
    private val dashboard: DashboardRepository,
    private val estrategias: EstrategiaRepository,
    private val ia: IaRepository
) : BaseViewModel() {

    private val _estado = MutableStateFlow<Estado<DadosDashboard>>(Estado.Carregando)
    val estado: StateFlow<Estado<DadosDashboard>> = _estado.asStateFlow()

    private val _estrategiaFiltro = MutableStateFlow<String?>(null)
    val estrategiaFiltro: StateFlow<String?> = _estrategiaFiltro.asStateFlow()

    /** Nulo enquanto a lideranca nao pediu os insights (cada pedido consome a cota da IA). */
    private val _insights = MutableStateFlow<Estado<InsightsIa>?>(null)
    val insights: StateFlow<Estado<InsightsIa>?> = _insights.asStateFlow()

    fun carregar() {
        val estrategiaId = _estrategiaFiltro.value
        _estado.carregar {
            dashboard.resumo(estrategiaId = estrategiaId)
                .combinar(dashboard.porEstrategia()) { resumo, porEstrategia -> resumo to porEstrategia }
                .combinar(dashboard.evolucao(estrategiaId = estrategiaId)) { (resumo, porEstrategia), evolucao ->
                    DadosDashboard(resumo, porEstrategia, evolucao)
                }
                .mapear { it.copy(saude = estrategias.saude().dadosOuNulo().orEmpty()) }
        }
    }

    fun filtrarPorEstrategia(estrategiaId: String?) {
        _estrategiaFiltro.value = estrategiaId
        carregar()
    }

    fun gerarInsights() {
        viewModelScope.launch {
            _insights.value = Estado.Carregando
            _insights.value = ia.insights().paraEstado()
        }
    }
}
