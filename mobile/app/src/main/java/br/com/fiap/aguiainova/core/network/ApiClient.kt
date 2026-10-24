package br.com.fiap.aguiainova.core.network

import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Monta o cliente HTTP (OkHttp + Retrofit + Gson) e cria as interfaces de API. */
class ApiClient(
    urlBase: () -> String,
    token: () -> String?,
    aoExpirarSessao: () -> Unit
) {

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(BaseUrlInterceptor(urlBase))
        .addInterceptor(AuthInterceptor(token, aoExpirarSessao))
        .connectTimeout(TEMPO_CONEXAO_SEGUNDOS, TimeUnit.SECONDS)
        // A IA generativa pode levar dezenas de segundos para responder.
        .readTimeout(TEMPO_LEITURA_SEGUNDOS, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(URL_TEMPORARIA)
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun <T> criar(api: Class<T>): T = retrofit.create(api)

    private companion object {
        /** Substituida a cada requisicao pelo BaseUrlInterceptor. */
        const val URL_TEMPORARIA = "http://localhost/"
        const val TEMPO_CONEXAO_SEGUNDOS = 15L
        // Cobre a subida a frio da API (hospedagem gratuita hiberna quando ociosa).
        const val TEMPO_LEITURA_SEGUNDOS = 120L
    }
}
