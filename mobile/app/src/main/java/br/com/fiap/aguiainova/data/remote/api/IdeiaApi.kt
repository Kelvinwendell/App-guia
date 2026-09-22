package br.com.fiap.aguiainova.data.remote.api

import br.com.fiap.aguiainova.domain.model.AvaliacaoIdeia
import br.com.fiap.aguiainova.domain.model.DadosIdeia
import br.com.fiap.aguiainova.domain.model.Ideia
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface IdeiaApi {

    @GET("api/ideias")
    suspend fun listar(
        @Query("status") status: String?,
        @Query("prioridade") prioridade: String?
    ): List<Ideia>

    @GET("api/ideias/aprovadas")
    suspend fun aprovadas(): List<Ideia>

    @GET("api/ideias/{id}")
    suspend fun obter(@Path("id") id: String): Ideia

    @POST("api/ideias")
    suspend fun criar(@Body dados: DadosIdeia): Ideia

    @PUT("api/ideias/{id}")
    suspend fun atualizar(@Path("id") id: String, @Body dados: DadosIdeia): Ideia

    @DELETE("api/ideias/{id}")
    suspend fun remover(@Path("id") id: String)

    @PATCH("api/ideias/{id}/avaliacao")
    suspend fun avaliar(@Path("id") id: String, @Body avaliacao: AvaliacaoIdeia): Ideia
}
