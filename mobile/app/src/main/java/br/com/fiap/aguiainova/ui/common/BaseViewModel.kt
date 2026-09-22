package br.com.fiap.aguiainova.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.aguiainova.core.network.Resultado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Comportamento comum dos ViewModels: carregar dados para um fluxo de [Estado], executar acoes de escrita e comunicar
 * o resultado a tela por meio de "avisos" (mensagens curtas) e do indicador [ocupado].
 */
abstract class BaseViewModel : ViewModel() {

    private val _aviso = MutableStateFlow<String?>(null)
    val aviso: StateFlow<String?> = _aviso.asStateFlow()

    private val _ocupado = MutableStateFlow(false)
    val ocupado: StateFlow<Boolean> = _ocupado.asStateFlow()

    fun avisoLido() {
        _aviso.value = null
    }

    protected fun avisar(mensagem: String) {
        _aviso.value = mensagem
    }

    /**
     * Carrega (ou recarrega) dados. Na primeira vez mostra "carregando"; nas seguintes mantem os dados na tela enquanto
     * atualiza e, se a atualizacao falhar, avisa sem descartar o que o usuario ja estava vendo.
     */
    protected fun <T> MutableStateFlow<Estado<T>>.carregar(busca: suspend () -> Resultado<T>) {
        viewModelScope.launch {
            val anterior = value
            if (anterior !is Estado.Pronto) value = Estado.Carregando
            when (val resultado = busca()) {
                is Resultado.Sucesso -> value = Estado.Pronto(resultado.dados)
                is Resultado.Falha ->
                    if (anterior is Estado.Pronto) avisar(resultado.mensagem) else value = Estado.Erro(resultado.mensagem)
            }
        }
    }

    /**
     * Executa uma acao de escrita: enquanto roda, [ocupado] fica verdadeiro; se falhar, a mensagem vira um aviso;
     * se funcionar, chama [aoSucesso] com os dados.
     */
    protected fun <T> executar(acao: suspend () -> Resultado<T>, aoSucesso: (T) -> Unit = {}) {
        viewModelScope.launch {
            _ocupado.value = true
            when (val resultado = acao()) {
                is Resultado.Sucesso -> aoSucesso(resultado.dados)
                is Resultado.Falha -> avisar(resultado.mensagem)
            }
            _ocupado.value = false
        }
    }
}
