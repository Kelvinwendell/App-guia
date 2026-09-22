package br.com.fiap.aguiainova.data.repository

import br.com.fiap.aguiainova.core.network.LeitorDeErroApi
import br.com.fiap.aguiainova.core.network.Resultado
import br.com.fiap.aguiainova.data.remote.api.DashboardApi
import br.com.fiap.aguiainova.domain.model.DesempenhoEstrategia
import br.com.fiap.aguiainova.domain.model.DesempenhoProjeto
import br.com.fiap.aguiainova.domain.model.EvolucaoMensal
import br.com.fiap.aguiainova.domain.model.ResumoDashboard
import br.com.fiap.aguiainova.domain.repository.DashboardRepository

class DashboardRepositoryRemoto(
    private val api: DashboardApi,
    leitorDeErro: LeitorDeErroApi
) : BaseRepositorio(leitorDeErro), DashboardRepository {

    override suspend fun resumo(estrategiaId: String?, projetoId: String?): Resultado<ResumoDashboard> =
        chamar { api.resumo(estrategiaId, projetoId) }

    override suspend fun porEstrategia(): Resultado<List<DesempenhoEstrategia>> = chamar { api.porEstrategia() }

    override suspend fun porProjeto(estrategiaId: String?): Resultado<List<DesempenhoProjeto>> =
        chamar { api.porProjeto(estrategiaId) }

    override suspend fun evolucao(estrategiaId: String?, projetoId: String?): Resultado<List<EvolucaoMensal>> =
        chamar { api.evolucao(estrategiaId, projetoId) }
}
