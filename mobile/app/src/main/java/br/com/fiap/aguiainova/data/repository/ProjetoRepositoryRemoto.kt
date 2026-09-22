package br.com.fiap.aguiainova.data.repository

import br.com.fiap.aguiainova.core.network.LeitorDeErroApi
import br.com.fiap.aguiainova.core.network.Resultado
import br.com.fiap.aguiainova.data.remote.api.ProjetoApi
import br.com.fiap.aguiainova.domain.model.AndamentoProjeto
import br.com.fiap.aguiainova.domain.model.DadosProjeto
import br.com.fiap.aguiainova.domain.model.DadosResultado
import br.com.fiap.aguiainova.domain.model.EtapaProjeto
import br.com.fiap.aguiainova.domain.model.Projeto
import br.com.fiap.aguiainova.domain.model.StatusProjeto
import br.com.fiap.aguiainova.domain.repository.ProjetoRepository

class ProjetoRepositoryRemoto(
    private val api: ProjetoApi,
    leitorDeErro: LeitorDeErroApi
) : BaseRepositorio(leitorDeErro), ProjetoRepository {

    override suspend fun listar(
        status: StatusProjeto?,
        etapa: EtapaProjeto?,
        estrategiaId: String?
    ): Resultado<List<Projeto>> = chamar { api.listar(status?.name, etapa?.name, estrategiaId) }

    override suspend fun obter(id: String): Resultado<Projeto> = chamar { api.obter(id) }

    override suspend fun criar(dados: DadosProjeto): Resultado<Projeto> = chamar { api.criar(dados) }

    override suspend fun atualizar(id: String, dados: DadosProjeto): Resultado<Projeto> =
        chamar { api.atualizar(id, dados) }

    override suspend fun atualizarAndamento(id: String, andamento: AndamentoProjeto): Resultado<Projeto> =
        chamar { api.atualizarAndamento(id, andamento) }

    override suspend fun adicionarResultado(id: String, dados: DadosResultado): Resultado<Projeto> =
        chamar { api.adicionarResultado(id, dados) }

    override suspend fun removerResultado(id: String, resultadoId: String): Resultado<Projeto> =
        chamar { api.removerResultado(id, resultadoId) }

    override suspend fun remover(id: String): Resultado<Unit> = chamar { api.remover(id) }
}
