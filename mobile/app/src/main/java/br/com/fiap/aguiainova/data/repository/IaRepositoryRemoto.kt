package br.com.fiap.aguiainova.data.repository

import br.com.fiap.aguiainova.core.network.LeitorDeErroApi
import br.com.fiap.aguiainova.core.network.Resultado
import br.com.fiap.aguiainova.data.remote.api.IaApi
import br.com.fiap.aguiainova.domain.model.AnaliseEstrategia
import br.com.fiap.aguiainova.domain.model.AvaliacaoIdeiaIa
import br.com.fiap.aguiainova.domain.model.DadosEstrategia
import br.com.fiap.aguiainova.domain.model.InsightsIa
import br.com.fiap.aguiainova.domain.model.LeituraSaude
import br.com.fiap.aguiainova.domain.model.MensagemChat
import br.com.fiap.aguiainova.domain.model.PedidoAnaliseEstrategia
import br.com.fiap.aguiainova.domain.model.PedidoChat
import br.com.fiap.aguiainova.domain.model.PedidoRascunhoEstrategia
import br.com.fiap.aguiainova.domain.model.RankingIa
import br.com.fiap.aguiainova.domain.model.RascunhoEstrategia
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

    override suspend fun rascunharEstrategia(objetivo: String): Resultado<RascunhoEstrategia> =
        chamar { api.rascunharEstrategia(PedidoRascunhoEstrategia(objetivo)) }

    override suspend fun analisarEstrategia(estrategiaId: String?, dados: DadosEstrategia): Resultado<AnaliseEstrategia> =
        chamar { api.analisarEstrategia(PedidoAnaliseEstrategia(estrategiaId, dados)) }

    override suspend fun lerSaude(estrategiaId: String): Resultado<LeituraSaude> = chamar { api.lerSaude(estrategiaId) }
}
