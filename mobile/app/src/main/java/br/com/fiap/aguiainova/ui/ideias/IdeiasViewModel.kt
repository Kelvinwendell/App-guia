package br.com.fiap.aguiainova.ui.ideias

import br.com.fiap.aguiainova.domain.model.AvaliacaoIdeia
import br.com.fiap.aguiainova.domain.model.Ideia
import br.com.fiap.aguiainova.domain.model.Perfil
import br.com.fiap.aguiainova.domain.model.StatusIdeia
import br.com.fiap.aguiainova.domain.repository.IaRepository
import br.com.fiap.aguiainova.domain.repository.IdeiaRepository
import br.com.fiap.aguiainova.ui.common.BaseViewModel
import br.com.fiap.aguiainova.ui.common.Estado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Lista de ideias para OPERADOR (as proprias ou as aprovadas da empresa) e GESTOR (todas, com IA). */
class IdeiasViewModel(
    private val perfil: Perfil,
    private val ideias: IdeiaRepository,
    private val ia: IaRepository
) : BaseViewModel() {

    private val _estado = MutableStateFlow<Estado<List<Ideia>>>(Estado.Carregando)
    val estado: StateFlow<Estado<List<Ideia>>> = _estado.asStateFlow()

    private val _filtro = MutableStateFlow<StatusIdeia?>(null)
    val filtro: StateFlow<StatusIdeia?> = _filtro.asStateFlow()

    private val _somenteAprovadas = MutableStateFlow(false)
    val somenteAprovadas: StateFlow<Boolean> = _somenteAprovadas.asStateFlow()

    private val _ordenarPorIa = MutableStateFlow(false)
    val ordenarPorIa: StateFlow<Boolean> = _ordenarPorIa.asStateFlow()

    fun carregar() {
        _estado.carregar {
            if (perfil == Perfil.OPERADOR && _somenteAprovadas.value) ideias.aprovadas()
            else ideias.listar(status = _filtro.value)
        }
    }

    fun filtrarPor(status: StatusIdeia?) {
        _filtro.value = status
        carregar()
    }

    fun mostrarAprovadasDaEmpresa(mostrar: Boolean) {
        _somenteAprovadas.value = mostrar
        carregar()
    }

    fun ordenarPorNotaDaIa(ordenar: Boolean) {
        _ordenarPorIa.value = ordenar
    }

    fun excluir(id: String) =
        executar({ ideias.remover(id) }) {
            avisar("Ideia excluída.")
            carregar()
        }

    fun decidir(id: String, avaliacao: AvaliacaoIdeia) =
        executar({ ideias.avaliar(id, avaliacao) }) {
            avisar("Ideia ${it.status.rotulo.lowercase()}.")
            carregar()
        }

    fun pontuarComIa(id: String) =
        executar({ ia.avaliarIdeia(id) }) {
            avisar("Nota atualizada (${it.avaliacao.score}).")
            carregar()
        }

    fun priorizarComIa() =
        executar({ ia.avaliarPendentes() }) {
            avisar(
                if (it.avaliadasAgora == 0) "Lista ordenada pela nota da IA."
                else "${it.avaliadasAgora} ideia(s) pontuada(s). Lista ordenada pela nota."
            )
            _ordenarPorIa.value = true
            carregar()
        }
}
