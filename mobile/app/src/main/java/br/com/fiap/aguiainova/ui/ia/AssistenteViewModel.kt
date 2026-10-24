package br.com.fiap.aguiainova.ui.ia

import androidx.lifecycle.viewModelScope
import br.com.fiap.aguiainova.core.network.dadosOuNulo
import br.com.fiap.aguiainova.domain.model.MensagemChat
import br.com.fiap.aguiainova.domain.model.PapelMensagem
import br.com.fiap.aguiainova.domain.model.StatusIa
import br.com.fiap.aguiainova.domain.repository.IaRepository
import br.com.fiap.aguiainova.ui.common.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Chat do gestor com a IA generativa. O servidor nao guarda a conversa: o app reenvia o historico a cada pergunta. */
class AssistenteViewModel(private val ia: IaRepository) : BaseViewModel() {

    private val _mensagens = MutableStateFlow<List<MensagemChat>>(emptyList())
    val mensagens: StateFlow<List<MensagemChat>> = _mensagens.asStateFlow()

    private val _status = MutableStateFlow<StatusIa?>(null)
    val status: StateFlow<StatusIa?> = _status.asStateFlow()

    /** Posicoes (em [mensagens]) das respostas feitas por regras, sem IA generativa: a tela rotula essas. */
    private val _respostasPorRegras = MutableStateFlow<Set<Int>>(emptySet())
    val respostasPorRegras: StateFlow<Set<Int>> = _respostasPorRegras.asStateFlow()

    /** Falso ate a consulta de status terminar; status nulo depois disso quer dizer "nao deu para saber". */
    private val _statusVerificado = MutableStateFlow(false)
    val statusVerificado: StateFlow<Boolean> = _statusVerificado.asStateFlow()

    fun carregarStatus() {
        viewModelScope.launch {
            _status.value = ia.status().dadosOuNulo()
            _statusVerificado.value = true
        }
    }

    fun enviar(texto: String) {
        val pergunta = texto.trim()
        // A UI desabilita o botao enquanto espera, mas o contrato de "uma pergunta por vez" e daqui: dois toques
        // rapidos chegavam antes da recomposicao e geravam duas chamadas (e cota) para a IA.
        if (pergunta.isEmpty() || ocupado.value) return
        val historico = _mensagens.value
        _mensagens.value = historico + MensagemChat(PapelMensagem.USUARIO, pergunta)
        executar({ ia.conversar(pergunta, historico.takeLast(LIMITE_HISTORICO)) }) {
            if (it.fonte == FONTE_REGRAS) _respostasPorRegras.value = _respostasPorRegras.value + _mensagens.value.size
            _mensagens.value = _mensagens.value + MensagemChat(PapelMensagem.ASSISTENTE, it.resposta)
        }
    }

    private companion object {
        /** A API aceita ate 20 mensagens de historico. */
        const val LIMITE_HISTORICO = 20
        const val FONTE_REGRAS = "HEURISTICA"
    }
}
