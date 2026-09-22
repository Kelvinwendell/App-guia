package br.com.fiap.aguiainova.data.repository

import br.com.fiap.aguiainova.core.network.LeitorDeErroApi
import br.com.fiap.aguiainova.core.network.Resultado
import kotlin.coroutines.cancellation.CancellationException

/**
 * Comportamento comum dos repositorios remotos: executa uma chamada de rede e converte qualquer falha (sem conexao,
 * erro HTTP, JSON inesperado) em [Resultado.Falha], para que nenhuma excecao chegue as telas.
 */
abstract class BaseRepositorio(private val leitorDeErro: LeitorDeErroApi) {

    protected suspend fun <T> chamar(chamada: suspend () -> T): Resultado<T> =
        try {
            Resultado.Sucesso(chamada())
        } catch (cancelamento: CancellationException) {
            throw cancelamento
        } catch (erro: Exception) {
            leitorDeErro.traduzir(erro)
        }
}
