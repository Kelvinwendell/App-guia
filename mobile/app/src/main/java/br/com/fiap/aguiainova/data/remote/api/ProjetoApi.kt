package br.com.fiap.aguiainova.data.remote.api

import br.com.fiap.aguiainova.domain.model.AndamentoProjeto
import br.com.fiap.aguiainova.domain.model.DadosProjeto
import br.com.fiap.aguiainova.domain.model.DadosResultado
import br.com.fiap.aguiainova.domain.model.Projeto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ProjetoApi {

    @GET("api/projetos")
    suspend fun listar(
        @Query("status") status: String?,
        @Query("etapa") etapa: String?,
        @Query("estrategiaId") estrategiaId: String?
    ): List<Projeto>

    @GET("api/projetos/{id}")
    suspend fun obter(@Path("id") id: String): Projeto

    @POST("api/projetos")
    suspend fun criar(@Body dados: DadosProjeto): Projeto

    @PUT("api/projetos/{id}")
    suspend fun atualizar(@Path("id") id: String, @Body dados: DadosProjeto): Projeto

    @PATCH("api/projetos/{id}/progresso")
    suspend fun atualizarAndamento(@Path("id") id: String, @Body andamento: AndamentoProjeto): Projeto

    @POST("api/projetos/{id}/resultados")
    suspend fun adicionarResultado(@Path("id") id: String, @Body dados: DadosResultado): Projeto

    @DELETE("api/projetos/{id}/resultados/{resultadoId}")
    suspend fun removerResultado(@Path("id") id: String, @Path("resultadoId") resultadoId: String): Projeto

    @DELETE("api/projetos/{id}")
    suspend fun remover(@Path("id") id: String)
}
