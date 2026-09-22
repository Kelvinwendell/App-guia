package br.com.fiap.aguiainova.core.di

import android.content.Context
import br.com.fiap.aguiainova.BuildConfig
import br.com.fiap.aguiainova.core.network.ApiClient
import br.com.fiap.aguiainova.core.network.LeitorDeErroApi
import br.com.fiap.aguiainova.core.session.ArmazenamentoSessaoPrefs
import br.com.fiap.aguiainova.core.session.ConfiguracaoServidor
import br.com.fiap.aguiainova.core.session.ConfiguracaoServidorPrefs
import br.com.fiap.aguiainova.core.session.GerenciadorSessao
import br.com.fiap.aguiainova.data.remote.api.AuthApi
import br.com.fiap.aguiainova.data.remote.api.DashboardApi
import br.com.fiap.aguiainova.data.remote.api.EstrategiaApi
import br.com.fiap.aguiainova.data.remote.api.IaApi
import br.com.fiap.aguiainova.data.remote.api.IdeiaApi
import br.com.fiap.aguiainova.data.remote.api.ProjetoApi
import br.com.fiap.aguiainova.data.repository.AuthRepositoryRemoto
import br.com.fiap.aguiainova.data.repository.DashboardRepositoryRemoto
import br.com.fiap.aguiainova.data.repository.EstrategiaRepositoryRemoto
import br.com.fiap.aguiainova.data.repository.IaRepositoryRemoto
import br.com.fiap.aguiainova.data.repository.IdeiaRepositoryRemoto
import br.com.fiap.aguiainova.data.repository.ProjetoRepositoryRemoto
import br.com.fiap.aguiainova.domain.repository.AuthRepository
import br.com.fiap.aguiainova.domain.repository.DashboardRepository
import br.com.fiap.aguiainova.domain.repository.EstrategiaRepository
import br.com.fiap.aguiainova.domain.repository.IaRepository
import br.com.fiap.aguiainova.domain.repository.IdeiaRepository
import br.com.fiap.aguiainova.domain.repository.ProjetoRepository

/**
 * Raiz de composicao: cria (uma vez) e conecta as pecas do app. Injecao de dependencia manual, suficiente para o
 * tamanho do projeto e sem gerar codigo; os ViewModels recebem daqui apenas as interfaces dos repositorios.
 */
class AppContainer(context: Context) {

    val sessao = GerenciadorSessao(ArmazenamentoSessaoPrefs(context))

    val servidor: ConfiguracaoServidor = ConfiguracaoServidorPrefs(context, BuildConfig.API_BASE_URL)

    private val leitorDeErro = LeitorDeErroApi()

    private val apiClient = ApiClient(
        urlBase = servidor::urlBase,
        token = sessao::token,
        aoExpirarSessao = sessao::encerrar
    )

    val auth: AuthRepository = AuthRepositoryRemoto(apiClient.criar(AuthApi::class.java), sessao, leitorDeErro)

    val estrategias: EstrategiaRepository =
        EstrategiaRepositoryRemoto(apiClient.criar(EstrategiaApi::class.java), leitorDeErro)

    val ideias: IdeiaRepository = IdeiaRepositoryRemoto(apiClient.criar(IdeiaApi::class.java), leitorDeErro)

    val projetos: ProjetoRepository = ProjetoRepositoryRemoto(apiClient.criar(ProjetoApi::class.java), leitorDeErro)

    val dashboard: DashboardRepository =
        DashboardRepositoryRemoto(apiClient.criar(DashboardApi::class.java), leitorDeErro)

    val ia: IaRepository = IaRepositoryRemoto(apiClient.criar(IaApi::class.java), leitorDeErro)
}
