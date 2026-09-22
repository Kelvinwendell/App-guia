package br.com.fiap.aguiainova.data.remote.api

import br.com.fiap.aguiainova.domain.model.AvaliacaoIdeiaIa
import br.com.fiap.aguiainova.domain.model.InsightsIa
import br.com.fiap.aguiainova.domain.model.PedidoChat
import br.com.fiap.aguiainova.domain.model.RankingIa
import br.com.fiap.aguiainova.domain.model.RespostaChat
import br.com.fiap.aguiainova.domain.model.StatusIa
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface IaApi {

    @GET("api/ia/status")
    suspend fun status(): StatusIa

    @POST("api/ia/ideias/{id}/avaliar")
    suspend fun avaliarIdeia(@Path("id") id: String): AvaliacaoIdeiaIa

    @POST("api/ia/ideias/avaliar-pendentes")
    suspend fun avaliarPendentes(): RankingIa

    @POST("api/ia/chat")
    suspend fun conversar(@Body pedido: PedidoChat): RespostaChat

    @POST("api/ia/dashboard/insights")
    suspend fun insights(): InsightsIa
}
