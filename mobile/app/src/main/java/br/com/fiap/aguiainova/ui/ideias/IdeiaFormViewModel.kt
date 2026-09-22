package br.com.fiap.aguiainova.ui.ideias

import br.com.fiap.aguiainova.core.network.Resultado
import br.com.fiap.aguiainova.core.network.combinar
import br.com.fiap.aguiainova.domain.model.DadosIdeia
import br.com.fiap.aguiainova.domain.model.Estrategia
import br.com.fiap.aguiainova.domain.model.Ideia
import br.com.fiap.aguiainova.domain.model.StatusEstrategia
import br.com.fiap.aguiainova.domain.repository.EstrategiaRepository
import br.com.fiap.aguiainova.domain.repository.IdeiaRepository
import br.com.fiap.aguiainova.ui.common.BaseViewModel
import br.com.fiap.aguiainova.ui.common.Estado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FormularioIdeia(val existente: Ideia?, val estrategiasVigentes: List<Estrategia>)

/** Criacao (id nulo) e edicao (id informado) de uma ideia, com a lista de estrategias vigentes para vincular. */
class IdeiaFormViewModel(
    private val id: String?,
    private val ideias: IdeiaRepository,
    private val estrategias: EstrategiaRepository
) : BaseViewModel() {

    private val _estado = MutableStateFlow<Estado<FormularioIdeia>>(Estado.Carregando)
    val estado: StateFlow<Estado<FormularioIdeia>> = _estado.asStateFlow()

    fun carregar() {
        _estado.carregar {
            val existente: Resultado<Ideia?> = if (id == null) Resultado.Sucesso(null) else ideias.obter(id)
            existente.combinar(estrategias.listar(StatusEstrategia.VIGENTE)) { ideia, vigentes ->
                FormularioIdeia(ideia, vigentes)
            }
        }
    }

    fun salvar(dados: DadosIdeia, aoSalvar: () -> Unit) {
        val camposPreenchidos = dados.titulo.isNotBlank() && dados.descricao.isNotBlank() && dados.categoria.isNotBlank()
        if (!camposPreenchidos) {
            avisar("Preencha título, descrição e categoria.")
            return
        }
        executar({ if (id == null) ideias.criar(dados) else ideias.atualizar(id, dados) }) { aoSalvar() }
    }
}
