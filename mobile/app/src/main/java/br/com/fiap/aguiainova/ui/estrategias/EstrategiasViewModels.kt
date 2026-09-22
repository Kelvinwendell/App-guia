package br.com.fiap.aguiainova.ui.estrategias

import br.com.fiap.aguiainova.core.network.Resultado
import br.com.fiap.aguiainova.domain.model.DadosEstrategia
import br.com.fiap.aguiainova.domain.model.Estrategia
import br.com.fiap.aguiainova.domain.model.EstrategiaHistorico
import br.com.fiap.aguiainova.domain.repository.EstrategiaRepository
import br.com.fiap.aguiainova.ui.common.BaseViewModel
import br.com.fiap.aguiainova.ui.common.Estado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EstrategiasViewModel(private val estrategias: EstrategiaRepository) : BaseViewModel() {

    private val _estado = MutableStateFlow<Estado<List<Estrategia>>>(Estado.Carregando)
    val estado: StateFlow<Estado<List<Estrategia>>> = _estado.asStateFlow()

    fun carregar() {
        _estado.carregar { estrategias.listar() }
    }

    fun excluir(id: String) =
        executar({ estrategias.remover(id) }) {
            avisar("Estratégia excluída.")
            carregar()
        }
}

/** Criacao (id nulo) e edicao de uma estrategia. */
class EstrategiaFormViewModel(
    private val id: String?,
    private val estrategias: EstrategiaRepository
) : BaseViewModel() {

    private val _estado = MutableStateFlow<Estado<Estrategia?>>(Estado.Carregando)
    val estado: StateFlow<Estado<Estrategia?>> = _estado.asStateFlow()

    fun carregar() {
        _estado.carregar { if (id == null) Resultado.Sucesso(null) else estrategias.obter(id) }
    }

    fun salvar(dados: DadosEstrategia, aoSalvar: () -> Unit) {
        if (dados.titulo.isBlank() || dados.descricao.isBlank() || dados.categoria.isBlank()) {
            avisar("Preencha título, descrição e categoria.")
            return
        }
        executar({ if (id == null) estrategias.criar(dados) else estrategias.atualizar(id, dados) }) { aoSalvar() }
    }
}

/** Historico de mudancas (id, data, categoria, campanha), filtravel por categoria e campanha. */
class HistoricoEstrategiasViewModel(private val estrategias: EstrategiaRepository) : BaseViewModel() {

    private val _estado = MutableStateFlow<Estado<List<EstrategiaHistorico>>>(Estado.Carregando)
    val estado: StateFlow<Estado<List<EstrategiaHistorico>>> = _estado.asStateFlow()

    fun carregar(categoria: String? = null, campanha: String? = null) {
        _estado.carregar { estrategias.historico(categoria, campanha) }
    }
}
