package br.com.fiap.aguiainova.ui.projetos

import br.com.fiap.aguiainova.core.network.combinar
import br.com.fiap.aguiainova.domain.model.AndamentoProjeto
import br.com.fiap.aguiainova.domain.model.DadosProjeto
import br.com.fiap.aguiainova.domain.model.DadosResultado
import br.com.fiap.aguiainova.domain.model.Estrategia
import br.com.fiap.aguiainova.domain.model.Ideia
import br.com.fiap.aguiainova.domain.model.Projeto
import br.com.fiap.aguiainova.domain.model.StatusEstrategia
import br.com.fiap.aguiainova.domain.repository.EstrategiaRepository
import br.com.fiap.aguiainova.domain.repository.IdeiaRepository
import br.com.fiap.aguiainova.domain.repository.ProjetoRepository
import br.com.fiap.aguiainova.ui.common.BaseViewModel
import br.com.fiap.aguiainova.ui.common.Estado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProjetosViewModel(private val projetos: ProjetoRepository) : BaseViewModel() {

    private val _estado = MutableStateFlow<Estado<List<Projeto>>>(Estado.Carregando)
    val estado: StateFlow<Estado<List<Projeto>>> = _estado.asStateFlow()

    fun carregar() {
        _estado.carregar { projetos.listar() }
    }
}

data class OpcoesDoProjeto(val estrategiasVigentes: List<Estrategia>, val ideiasAprovadas: List<Ideia>)

/** Cadastro de um projeto/iniciativa: carrega as estrategias vigentes e as ideias aprovadas para vincular. */
class ProjetoFormViewModel(
    private val projetos: ProjetoRepository,
    private val estrategias: EstrategiaRepository,
    private val ideias: IdeiaRepository
) : BaseViewModel() {

    private val _estado = MutableStateFlow<Estado<OpcoesDoProjeto>>(Estado.Carregando)
    val estado: StateFlow<Estado<OpcoesDoProjeto>> = _estado.asStateFlow()

    fun carregar() {
        _estado.carregar {
            estrategias.listar(StatusEstrategia.VIGENTE).combinar(ideias.aprovadas()) { vigentes, aprovadas ->
                OpcoesDoProjeto(vigentes, aprovadas)
            }
        }
    }

    fun criar(dados: DadosProjeto, aoCriar: (Projeto) -> Unit) {
        executar({ projetos.criar(dados) }, aoCriar)
    }

    fun avisarCampoInvalido(mensagem: String) = avisar(mensagem)
}

/** Detalhe de um projeto: o GESTOR atualiza andamento e resultados; a LIDERANCA so consulta. */
class ProjetoDetalheViewModel(
    private val id: String,
    private val projetos: ProjetoRepository
) : BaseViewModel() {

    private val _estado = MutableStateFlow<Estado<Projeto>>(Estado.Carregando)
    val estado: StateFlow<Estado<Projeto>> = _estado.asStateFlow()

    fun carregar() {
        _estado.carregar { projetos.obter(id) }
    }

    fun salvarAndamento(andamento: AndamentoProjeto) =
        executar({ projetos.atualizarAndamento(id, andamento) }) {
            _estado.value = Estado.Pronto(it)
            avisar("Andamento atualizado.")
        }

    fun adicionarResultado(dados: DadosResultado) =
        executar({ projetos.adicionarResultado(id, dados) }) {
            _estado.value = Estado.Pronto(it)
            avisar("Resultado registrado.")
        }

    fun removerResultado(resultadoId: String) =
        executar({ projetos.removerResultado(id, resultadoId) }) {
            _estado.value = Estado.Pronto(it)
            avisar("Resultado removido.")
        }

    fun excluir(aoExcluir: () -> Unit) =
        executar({ projetos.remover(id) }) { aoExcluir() }

    fun avisarCampoInvalido(mensagem: String) = avisar(mensagem)
}
