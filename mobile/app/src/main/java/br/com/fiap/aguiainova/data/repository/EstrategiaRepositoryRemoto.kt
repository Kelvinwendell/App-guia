package br.com.fiap.aguiainova.data.repository

import br.com.fiap.aguiainova.core.network.LeitorDeErroApi
import br.com.fiap.aguiainova.core.network.Resultado
import br.com.fiap.aguiainova.data.remote.api.EstrategiaApi
import br.com.fiap.aguiainova.domain.model.DadosEstrategia
import br.com.fiap.aguiainova.domain.model.Estrategia
import br.com.fiap.aguiainova.domain.model.EstrategiaHistorico
import br.com.fiap.aguiainova.domain.model.StatusEstrategia
import br.com.fiap.aguiainova.domain.repository.EstrategiaRepository

class EstrategiaRepositoryRemoto(
    private val api: EstrategiaApi,
    leitorDeErro: LeitorDeErroApi
) : BaseRepositorio(leitorDeErro), EstrategiaRepository {

    override suspend fun listar(status: StatusEstrategia?): Resultado<List<Estrategia>> =
        chamar { api.listar(status?.name) }

    override suspend fun obter(id: String): Resultado<Estrategia> = chamar { api.obter(id) }

    override suspend fun criar(dados: DadosEstrategia): Resultado<Estrategia> = chamar { api.criar(dados) }

    override suspend fun atualizar(id: String, dados: DadosEstrategia): Resultado<Estrategia> =
        chamar { api.atualizar(id, dados) }

    override suspend fun remover(id: String): Resultado<Unit> = chamar { api.remover(id) }

    override suspend fun historico(categoria: String?, campanha: String?): Resultado<List<EstrategiaHistorico>> =
        chamar { api.historico(categoria?.takeIf { it.isNotBlank() }, campanha?.takeIf { it.isNotBlank() }) }
}
