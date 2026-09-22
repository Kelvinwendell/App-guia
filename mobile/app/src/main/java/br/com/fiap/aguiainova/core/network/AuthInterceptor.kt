package br.com.fiap.aguiainova.core.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Anexa o token JWT a todas as requisicoes e detecta sessao expirada: um 401 em resposta a uma requisicao que
 * levava token (fora do proprio login) significa que o token venceu ou foi revogado.
 */
class AuthInterceptor(
    private val token: () -> String?,
    private val aoExpirar: () -> Unit
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val tokenAtual = token()
        val requisicao = if (tokenAtual != null) {
            original.newBuilder().header("Authorization", "Bearer $tokenAtual").build()
        } else {
            original
        }
        val resposta = chain.proceed(requisicao)
        if (resposta.code == 401 && tokenAtual != null && !original.url.encodedPath.endsWith(CAMINHO_LOGIN)) {
            aoExpirar()
        }
        return resposta
    }

    private companion object {
        const val CAMINHO_LOGIN = "/api/auth/login"
    }
}
