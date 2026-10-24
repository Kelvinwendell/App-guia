package br.com.fiap.aguiainova.data.remote.api

import br.com.fiap.aguiainova.domain.model.DadosEstrategia
import br.com.fiap.aguiainova.domain.model.Estrategia
import br.com.fiap.aguiainova.domain.model.EstrategiaHistorico
import br.com.fiap.aguiainova.domain.model.SaudeEstrategia
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface EstrategiaApi {

    @GET("api/estrategias")
    suspend fun listar(@Query("status") status: String?): List<Estrategia>

    @GET("api/estrategias/{id}")
    suspend fun obter(@Path("id") id: String): Estrategia

    @POST("api/estrategias")
    suspend fun criar(@Body dados: DadosEstrategia): Estrategia

    @PUT("api/estrategias/{id}")
    suspend fun atualizar(@Path("id") id: String, @Body dados: DadosEstrategia): Estrategia

    @DELETE("api/estrategias/{id}")
    suspend fun remover(@Path("id") id: String)

    @GET("api/estrategias/historico")
    suspend fun historico(
        @Query("categoria") categoria: String?,
        @Query("campanha") campanha: String?
    ): List<EstrategiaHistorico>

    @GET("api/estrategias/saude")
    suspend fun saude(): List<SaudeEstrategia>

    @GET("api/estrategias/{id}/saude")
    suspend fun saudeDe(@Path("id") id: String): SaudeEstrategia
}
