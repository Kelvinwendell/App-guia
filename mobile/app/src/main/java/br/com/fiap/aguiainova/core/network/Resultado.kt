package br.com.fiap.aguiainova.core.network

enum class TipoErro {
    REDE,
    NAO_AUTENTICADO,
    PROIBIDO,
    NAO_ENCONTRADO,
    VALIDACAO,
    INDISPONIVEL,
    SERVIDOR,
    DESCONHECIDO
}

/** Resultado de uma operacao remota: ou os dados, ou uma falha ja traduzida para uma mensagem legivel. */
sealed interface Resultado<out T> {

    data class Sucesso<out T>(val dados: T) : Resultado<T>

    data class Falha(val tipo: TipoErro, val mensagem: String) : Resultado<Nothing>
}

inline fun <T, R> Resultado<T>.mapear(transformar: (T) -> R): Resultado<R> = when (this) {
    is Resultado.Sucesso -> Resultado.Sucesso(transformar(dados))
    is Resultado.Falha -> this
}

inline fun <T> Resultado<T>.aoSucesso(acao: (T) -> Unit): Resultado<T> {
    if (this is Resultado.Sucesso) acao(dados)
    return this
}

inline fun <T> Resultado<T>.aoFalhar(acao: (Resultado.Falha) -> Unit): Resultado<T> {
    if (this is Resultado.Falha) acao(this)
    return this
}

fun <T> Resultado<T>.dadosOuNulo(): T? = (this as? Resultado.Sucesso)?.dados

/** Junta dois resultados: falha se qualquer um falhar; senao combina os dados. */
inline fun <A, B, R> Resultado<A>.combinar(outro: Resultado<B>, juntar: (A, B) -> R): Resultado<R> = when {
    this is Resultado.Falha -> this
    outro is Resultado.Falha -> outro
    else -> Resultado.Sucesso(juntar((this as Resultado.Sucesso).dados, (outro as Resultado.Sucesso).dados))
}
