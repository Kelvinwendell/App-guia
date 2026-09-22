package br.com.fiap.aguiainova.data.repository

import br.com.fiap.aguiainova.core.network.LeitorDeErroApi
import br.com.fiap.aguiainova.core.network.Resultado
import br.com.fiap.aguiainova.data.remote.api.IaApi
import br.com.fiap.aguiainova.domain.model.AvaliacaoIdeiaIa
import br.com.fiap.aguiainova.domain.model.InsightsIa
import br.com.fiap.aguiainova.domain.model.MensagemChat
import br.com.fiap.aguiainova.domain.model.PedidoChat
import br.com.fiap.aguiainova.domain.model.RankingIa
import br.com.fiap.aguiainova.domain.model.RespostaChat
import br.com.fiap.aguiainova.domain.model.StatusIa
import br.com.fiap.aguiainova.domain.repository.IaRepository

class IaRepositoryRemoto(
    private val api: IaApi,
    leitorDeErro: LeitorDeErroApi
) : BaseRepositorio(leitorDeErro), IaRepository {

    override suspend fun status(): Resultado<StatusIa> = chamar { api.status() }

    override suspend fun avaliarIdeia(ideiaId: String): Resultado<AvaliacaoIdeiaIa> =
        chamar { api.avaliarIdeia(ideiaId) }

    override suspend fun avaliarPendentes(): Resultado<RankingIa> = chamar { api.avaliarPendentes() }

    override suspend fun conversar(mensagem: String, historico: List<MensagemChat>): Resultado<RespostaChat> =
        chamar { api.conversar(PedidoChat(mensagem, historico)) }

    override suspend fun insights(): Resultado<InsightsIa> = chamar { api.insights() }
}
