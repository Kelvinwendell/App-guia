package br.com.fiap.aguiainova.core.network

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Aponta cada requisicao para o servidor configurado no momento. O Retrofit exige uma URL base fixa na criacao;
 * este interceptor troca esquema, host, porta e prefixo de caminho, permitindo mudar o servidor sem recriar o cliente
 * (util no aparelho fisico, cujo IP do computador muda de rede para rede).
 */
class BaseUrlInterceptor(private val urlBase: () -> String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val base = urlBase().toHttpUrlOrNull() ?: return chain.proceed(original)
        val prefixo = base.encodedPath.trimEnd('/')
        val novaUrl = original.url.newBuilder()
            .scheme(base.scheme)
            .host(base.host)
            .port(base.port)
            .encodedPath(prefixo + original.url.encodedPath)
            .build()
        return chain.proceed(original.newBuilder().url(novaUrl).build())
    }
}
