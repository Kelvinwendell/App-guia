package br.com.fiap.aguiainova.data.repository

import br.com.fiap.aguiainova.core.network.LeitorDeErroApi
import br.com.fiap.aguiainova.core.network.Resultado
import br.com.fiap.aguiainova.data.remote.api.IdeiaApi
import br.com.fiap.aguiainova.domain.model.AvaliacaoIdeia
import br.com.fiap.aguiainova.domain.model.DadosIdeia
import br.com.fiap.aguiainova.domain.model.Ideia
import br.com.fiap.aguiainova.domain.model.Prioridade
import br.com.fiap.aguiainova.domain.model.StatusIdeia
import br.com.fiap.aguiainova.domain.repository.IdeiaRepository

class IdeiaRepositoryRemoto(
    private val api: IdeiaApi,
    leitorDeErro: LeitorDeErroApi
) : BaseRepositorio(leitorDeErro), IdeiaRepository {

    override suspend fun listar(status: StatusIdeia?, prioridade: Prioridade?): Resultado<List<Ideia>> =
        chamar { api.listar(status?.name, prioridade?.name) }

    override suspend fun aprovadas(): Resultado<List<Ideia>> = chamar { api.aprovadas() }

    override suspend fun obter(id: String): Resultado<Ideia> = chamar { api.obter(id) }

    override suspend fun criar(dados: DadosIdeia): Resultado<Ideia> = chamar { api.criar(dados) }

    override suspend fun atualizar(id: String, dados: DadosIdeia): Resultado<Ideia> =
        chamar { api.atualizar(id, dados) }

    override suspend fun remover(id: String): Resultado<Unit> = chamar { api.remover(id) }

    override suspend fun avaliar(id: String, avaliacao: AvaliacaoIdeia): Resultado<Ideia> =
        chamar { api.avaliar(id, avaliacao) }
}
