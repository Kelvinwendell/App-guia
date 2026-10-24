package br.com.fiap.aguiainova.ui.estrategias

import androidx.lifecycle.viewModelScope
import br.com.fiap.aguiainova.core.network.Resultado
import br.com.fiap.aguiainova.core.network.dadosOuNulo
import br.com.fiap.aguiainova.domain.model.AnaliseEstrategia
import br.com.fiap.aguiainova.domain.model.DadosEstrategia
import br.com.fiap.aguiainova.domain.model.Estrategia
import br.com.fiap.aguiainova.domain.model.EstrategiaHistorico
import br.com.fiap.aguiainova.domain.model.LeituraSaude
import br.com.fiap.aguiainova.domain.model.NivelSaude
import br.com.fiap.aguiainova.domain.model.Perfil
import br.com.fiap.aguiainova.domain.model.RascunhoEstrategia
import br.com.fiap.aguiainova.domain.model.SaudeEstrategia
import br.com.fiap.aguiainova.domain.repository.EstrategiaRepository
import br.com.fiap.aguiainova.domain.repository.IaRepository
import br.com.fiap.aguiainova.ui.common.BaseViewModel
import br.com.fiap.aguiainova.ui.common.Estado
import br.com.fiap.aguiainova.ui.common.paraEstado
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** As estrategias da empresa e, para quem pode ver, a saude de cada vigente (indexada pelo id da estrategia). */
data class PainelEstrategias(
    val estrategias: List<Estrategia>,
    val saude: Map<String, SaudeEstrategia> = emptyMap()
) {
    val vigentes: List<Estrategia> get() = estrategias.filter { it.vigente }

    val encerradas: List<Estrategia> get() = estrategias.filter { !it.vigente }

    /** Quantas estrategias vigentes ha em cada nivel de saude (so os niveis que aparecem). */
    fun contagemPorNivel(): Map<NivelSaude, Int> =
        vigentes.mapNotNull { saude[it.id]?.nivel }.groupingBy { it }.eachCount()
}

/** Lista de estrategias. A saude so e buscada para GESTOR e LIDERANCA; se ela falhar, a lista continua util. */
class EstrategiasViewModel(
    private val perfil: Perfil,
    private val estrategias: EstrategiaRepository
) : BaseViewModel() {

    private val _estado = MutableStateFlow<Estado<PainelEstrategias>>(Estado.Carregando)
    val estado: StateFlow<Estado<PainelEstrategias>> = _estado.asStateFlow()

    fun carregar() {
        _estado.carregar { buscar() }
    }

    private suspend fun buscar(): Resultado<PainelEstrategias> = coroutineScope {
        val lista = async { estrategias.listar() }
        val saude = async { if (perfil == Perfil.OPERADOR) null else estrategias.saude() }
        when (val resultado = lista.await()) {
            is Resultado.Falha -> resultado
            is Resultado.Sucesso -> Resultado.Sucesso(
                PainelEstrategias(
                    resultado.dados,
                    saude.await()?.dadosOuNulo().orEmpty().associateBy { it.estrategiaId }
                )
            )
        }
    }
}

data class DetalheEstrategia(val estrategia: Estrategia, val saude: SaudeEstrategia?)

/** Uma estrategia por inteiro: o plano (metas e periodo) e, para GESTOR e LIDERANCA, a saude e a leitura da IA. */
class EstrategiaDetalheViewModel(
    private val id: String,
    private val perfil: Perfil,
    private val estrategias: EstrategiaRepository,
    private val ia: IaRepository
) : BaseViewModel() {

    private val _estado = MutableStateFlow<Estado<DetalheEstrategia>>(Estado.Carregando)
    val estado: StateFlow<Estado<DetalheEstrategia>> = _estado.asStateFlow()

    /** Nulo enquanto ninguem pediu a leitura (cada pedido consome a cota da IA). */
    private val _leitura = MutableStateFlow<Estado<LeituraSaude>?>(null)
    val leitura: StateFlow<Estado<LeituraSaude>?> = _leitura.asStateFlow()

    fun carregar() {
        _estado.carregar { buscar() }
    }

    private suspend fun buscar(): Resultado<DetalheEstrategia> =
        when (val resultado = estrategias.obter(id)) {
            is Resultado.Falha -> resultado
            is Resultado.Sucesso -> {
                val estrategia = resultado.dados
                val saude = if (perfil != Perfil.OPERADOR && estrategia.vigente) estrategias.saudeDe(id).dadosOuNulo() else null
                Resultado.Sucesso(DetalheEstrategia(estrategia, saude))
            }
        }

    fun lerComIa() {
        viewModelScope.launch {
            _leitura.value = Estado.Carregando
            _leitura.value = ia.lerSaude(id).paraEstado()
        }
    }

    fun excluir(aoExcluir: () -> Unit) {
        executar({ estrategias.remover(id) }) { aoExcluir() }
    }
}

/** Criacao (id nulo) e edicao de uma estrategia, com rascunho e critica feitos pela IA. */
class EstrategiaFormViewModel(
    private val id: String?,
    private val estrategias: EstrategiaRepository,
    private val ia: IaRepository
) : BaseViewModel() {

    private val _estado = MutableStateFlow<Estado<Estrategia?>>(Estado.Carregando)
    val estado: StateFlow<Estado<Estrategia?>> = _estado.asStateFlow()

    /** Nulo ate a lideranca pedir a analise. */
    private val _analise = MutableStateFlow<Estado<AnaliseEstrategia>?>(null)
    val analise: StateFlow<Estado<AnaliseEstrategia>?> = _analise.asStateFlow()

    fun carregar() {
        _estado.carregar { if (id == null) Resultado.Sucesso(null) else estrategias.obter(id) }
    }

    fun avisarCampoInvalido(mensagem: String) = avisar(mensagem)

    /** Pede um rascunho a IA; quem chama preenche o formulario com ele. A lideranca revisa antes de salvar. */
    fun rascunhar(objetivo: String, aoPronto: (RascunhoEstrategia) -> Unit) {
        if (objetivo.isBlank()) {
            avisar("Descreva o objetivo para a IA escrever o rascunho.")
            return
        }
        executar({ ia.rascunharEstrategia(objetivo.trim()) }) {
            aoPronto(it)
            avisar("Rascunho gerado por IA. Revise antes de salvar.")
        }
    }

    fun analisar(dados: DadosEstrategia) {
        if (!camposObrigatoriosPreenchidos(dados)) {
            avisar("Preencha título, descrição e categoria para analisar.")
            return
        }
        viewModelScope.launch {
            _analise.value = Estado.Carregando
            _analise.value = ia.analisarEstrategia(id, dados).paraEstado()
        }
    }

    fun salvar(dados: DadosEstrategia, aoSalvar: () -> Unit) {
        if (!camposObrigatoriosPreenchidos(dados)) {
            avisar("Preencha título, descrição e categoria.")
            return
        }
        executar({ if (id == null) estrategias.criar(dados) else estrategias.atualizar(id, dados) }) { aoSalvar() }
    }

    private fun camposObrigatoriosPreenchidos(dados: DadosEstrategia) =
        dados.titulo.isNotBlank() && dados.descricao.isNotBlank() && dados.categoria.isNotBlank()
}

/** Filtro do historico; nulo em um campo quer dizer "todas". */
data class FiltroHistorico(val categoria: String? = null, val campanha: String? = null) {
    val vazio: Boolean get() = categoria == null && campanha == null
}

/** Opcoes dos seletores de filtro, tiradas do historico completo (a lista filtrada encolheria as opcoes). */
data class OpcoesHistorico(val categorias: List<String> = emptyList(), val campanhas: List<String> = emptyList())

/**
 * Historico de mudancas (id, data, categoria, campanha). O filtro mora aqui e vai para o repositorio, para que a API
 * filtre e para que "tentar novamente" repita a mesma busca.
 */
class HistoricoEstrategiasViewModel(private val estrategias: EstrategiaRepository) : BaseViewModel() {

    private val _estado = MutableStateFlow<Estado<List<EstrategiaHistorico>>>(Estado.Carregando)
    val estado: StateFlow<Estado<List<EstrategiaHistorico>>> = _estado.asStateFlow()

    private val _filtro = MutableStateFlow(FiltroHistorico())
    val filtro: StateFlow<FiltroHistorico> = _filtro.asStateFlow()

    private val _opcoes = MutableStateFlow(OpcoesHistorico())
    val opcoes: StateFlow<OpcoesHistorico> = _opcoes.asStateFlow()

    fun carregar() {
        val filtro = _filtro.value
        _estado.carregar {
            estrategias.historico(filtro.categoria, filtro.campanha).also { resultado ->
                if (filtro.vazio && resultado is Resultado.Sucesso) {
                    _opcoes.value = OpcoesHistorico(
                        resultado.dados.map { it.categoria }.distinct().sorted(),
                        resultado.dados.mapNotNull { it.campanha }.distinct().sorted()
                    )
                }
            }
        }
    }

    fun filtrarPor(categoria: String?, campanha: String?) {
        val novo = FiltroHistorico(categoria, campanha)
        if (novo == _filtro.value) return
        _filtro.value = novo
        carregar()
    }
}
