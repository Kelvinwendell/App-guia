package br.com.fiap.aguiainova.core.network

import com.google.gson.JsonParser
import java.io.IOException
import retrofit2.HttpException

/**
 * Traduz excecoes de rede e respostas de erro da API em [Resultado.Falha]. A API responde erros no formato
 * ProblemDetail (RFC 7807): o campo "detail" ja vem em portugues e pronto para ser mostrado ao usuario.
 */
class LeitorDeErroApi {

    fun traduzir(erro: Throwable): Resultado.Falha = when (erro) {
        is HttpException -> falhaHttp(erro)
        is IOException -> Resultado.Falha(
            TipoErro.REDE,
            "Sem conexão com o servidor. Verifique a internet e o endereço do servidor."
        )
        else -> Resultado.Falha(TipoErro.DESCONHECIDO, erro.message ?: "Erro inesperado.")
    }

    private fun falhaHttp(erro: HttpException): Resultado.Falha {
        val detalhe = extrairDetalhe(erro.response()?.errorBody()?.string())
        val codigo = erro.code()
        return Resultado.Falha(tipoDe(codigo), detalhe ?: mensagemPadrao(codigo))
    }

    private fun tipoDe(codigo: Int): TipoErro = when (codigo) {
        400, 409, 422 -> TipoErro.VALIDACAO
        401 -> TipoErro.NAO_AUTENTICADO
        403 -> TipoErro.PROIBIDO
        404 -> TipoErro.NAO_ENCONTRADO
        503 -> TipoErro.INDISPONIVEL
        in 500..599 -> TipoErro.SERVIDOR
        else -> TipoErro.DESCONHECIDO
    }

    private fun mensagemPadrao(codigo: Int): String = when (tipoDe(codigo)) {
        TipoErro.NAO_AUTENTICADO -> "Sessão expirada. Faça login novamente."
        TipoErro.PROIBIDO -> "Seu perfil não tem permissão para esta operação."
        TipoErro.NAO_ENCONTRADO -> "Registro não encontrado."
        TipoErro.INDISPONIVEL -> "Serviço indisponível no momento."
        TipoErro.SERVIDOR -> "Erro no servidor. Tente novamente mais tarde."
        else -> "Não foi possível concluir a operação (código $codigo)."
    }

    private fun extrairDetalhe(corpo: String?): String? {
        if (corpo.isNullOrBlank()) return null
        return runCatching {
            JsonParser.parseString(corpo).asJsonObject.get("detail")?.takeIf { !it.isJsonNull }?.asString
        }.getOrNull()
    }
}
