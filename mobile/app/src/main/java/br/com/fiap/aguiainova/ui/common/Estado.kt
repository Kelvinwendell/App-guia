package br.com.fiap.aguiainova.ui.common

import br.com.fiap.aguiainova.core.network.Resultado

/** Estado de uma tela que carrega dados: carregando, erro (com mensagem) ou pronta. */
sealed interface Estado<out T> {

    data object Carregando : Estado<Nothing>

    data class Erro(val mensagem: String) : Estado<Nothing>

    data class Pronto<out T>(val dados: T) : Estado<T>
}

fun <T> Resultado<T>.paraEstado(): Estado<T> = when (this) {
    is Resultado.Sucesso -> Estado.Pronto(dados)
    is Resultado.Falha -> Estado.Erro(mensagem)
}
