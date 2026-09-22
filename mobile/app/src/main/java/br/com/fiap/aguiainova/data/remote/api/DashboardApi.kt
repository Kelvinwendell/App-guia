package br.com.fiap.aguiainova.data.remote.api

import br.com.fiap.aguiainova.domain.model.DesempenhoEstrategia
import br.com.fiap.aguiainova.domain.model.DesempenhoProjeto
import br.com.fiap.aguiainova.domain.model.EvolucaoMensal
import br.com.fiap.aguiainova.domain.model.ResumoDashboard
import retrofit2.http.GET
import retrofit2.http.Query

interface DashboardApi {

    @GET("api/dashboard/resumo")
    suspend fun resumo(
        @Query("estrategiaId") estrategiaId: String?,
        @Query("projetoId") projetoId: String?
    ): ResumoDashboard

    @GET("api/dashboard/estrategias")
    suspend fun porEstrategia(): List<DesempenhoEstrategia>

    @GET("api/dashboard/projetos")
    suspend fun porProjeto(@Query("estrategiaId") estrategiaId: String?): List<DesempenhoProjeto>

    @GET("api/dashboard/evolucao")
    suspend fun evolucao(
        @Query("estrategiaId") estrategiaId: String?,
        @Query("projetoId") projetoId: String?
    ): List<EvolucaoMensal>
}
