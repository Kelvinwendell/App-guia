package br.com.fiap.aguiainova.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.fiap.aguiainova.core.di.AppContainer
import br.com.fiap.aguiainova.domain.model.Perfil
import br.com.fiap.aguiainova.ui.auth.LoginScreen
import br.com.fiap.aguiainova.ui.auth.RegistroScreen
import br.com.fiap.aguiainova.ui.dashboard.DashboardScreen
import br.com.fiap.aguiainova.ui.estrategias.EstrategiaDetalheScreen
import br.com.fiap.aguiainova.ui.estrategias.EstrategiaFormScreen
import br.com.fiap.aguiainova.ui.estrategias.EstrategiasScreen
import br.com.fiap.aguiainova.ui.home.HomeGestorScreen
import br.com.fiap.aguiainova.ui.home.HomeLiderancaScreen
import br.com.fiap.aguiainova.ui.home.HomeOperadorScreen
import br.com.fiap.aguiainova.ui.ideias.IdeiaFormScreen
import br.com.fiap.aguiainova.ui.ideias.IdeiasScreen
import br.com.fiap.aguiainova.ui.perfil.PerfilScreen
import br.com.fiap.aguiainova.ui.projetos.ProjetoDetalheScreen
import br.com.fiap.aguiainova.ui.projetos.ProjetoFormScreen
import br.com.fiap.aguiainova.ui.projetos.ProjetosScreen

/**
 * Mapa de navegacao do app. O destino inicial e a tela de login, ou a home do perfil quando ja existe uma sessao
 * salva; se a sessao terminar (logout ou token expirado), o app volta ao login de qualquer tela.
 */
@Composable
fun NavGraph(container: AppContainer, navController: NavHostController = rememberNavController()) {
    val usuario by container.sessao.usuario.collectAsStateWithLifecycle()
    val destinoInicial = remember { container.sessao.usuario.value?.let { Rotas.homeDe(it.perfil) } ?: Rotas.LOGIN }

    LaunchedEffect(usuario) {
        val rotaAtual = navController.currentDestination?.route
        if (usuario == null && rotaAtual != null && rotaAtual != Rotas.LOGIN && rotaAtual != Rotas.REGISTRO) {
            navController.navigate(Rotas.LOGIN) { popUpTo(0) { inclusive = true } }
        }
    }

    /** Telas autenticadas so sao compostas enquanto ha usuario; recebem o perfil dele. */
    @Composable
    fun ComPerfil(conteudo: @Composable (Perfil) -> Unit) {
        usuario?.perfil?.let { conteudo(it) }
    }

    NavHost(navController = navController, startDestination = destinoInicial) {
        composable(Rotas.LOGIN) {
            LoginScreen(
                aoEntrar = { perfil ->
                    navController.navigate(Rotas.homeDe(perfil)) { popUpTo(Rotas.LOGIN) { inclusive = true } }
                },
                aoCriarConta = { navController.navigate(Rotas.REGISTRO) }
            )
        }
        composable(Rotas.REGISTRO) {
            RegistroScreen(aoConcluir = { navController.popBackStack() }, aoVoltar = { navController.popBackStack() })
        }

        composable(Rotas.HOME_OPERADOR) { ComPerfil { HomeOperadorScreen(navController) } }
        composable(Rotas.HOME_GESTOR) { ComPerfil { HomeGestorScreen(navController) } }
        composable(Rotas.HOME_LIDERANCA) { ComPerfil { HomeLiderancaScreen(navController) } }

        composable(Rotas.IDEIAS) { ComPerfil { IdeiasScreen(it, navController) } }
        composable(Rotas.PADRAO_IDEIA_FORM, arguments = argumentoOpcional()) { entrada ->
            IdeiaFormScreen(entrada.arguments?.getString(Rotas.ARGUMENTO_ID)) { navController.popBackStack() }
        }

        composable(
            Rotas.PADRAO_ESTRATEGIAS,
            arguments = argumentoOpcional(Rotas.ARGUMENTO_ABA)
        ) { entrada ->
            ComPerfil { EstrategiasScreen(it, navController, entrada.arguments?.getString(Rotas.ARGUMENTO_ABA)) }
        }
        composable(Rotas.PADRAO_ESTRATEGIA_FORM, arguments = argumentoOpcional()) { entrada ->
            EstrategiaFormScreen(entrada.arguments?.getString(Rotas.ARGUMENTO_ID)) { navController.popBackStack() }
        }
        composable(
            Rotas.PADRAO_ESTRATEGIA_DETALHE,
            arguments = listOf(navArgument(Rotas.ARGUMENTO_ID) { type = NavType.StringType })
        ) { entrada ->
            val id = entrada.arguments?.getString(Rotas.ARGUMENTO_ID) ?: return@composable
            ComPerfil { EstrategiaDetalheScreen(id, it, navController) }
        }

        composable(Rotas.PROJETOS) { ComPerfil { ProjetosScreen(it, navController) } }
        composable(Rotas.PROJETO_NOVO) { ProjetoFormScreen { navController.popBackStack() } }
        composable(
            Rotas.PADRAO_PROJETO_DETALHE,
            arguments = listOf(navArgument(Rotas.ARGUMENTO_ID) { type = NavType.StringType })
        ) { entrada ->
            val id = entrada.arguments?.getString(Rotas.ARGUMENTO_ID) ?: return@composable
            ComPerfil { ProjetoDetalheScreen(id, it) { navController.popBackStack() } }
        }

        composable(Rotas.DASHBOARD) { DashboardScreen(navController) }
        composable(Rotas.PERFIL) { ComPerfil { PerfilScreen(it, navController) } }
    }
}

private fun argumentoOpcional(nome: String = Rotas.ARGUMENTO_ID) = listOf(
    navArgument(nome) {
        type = NavType.StringType
        nullable = true
        defaultValue = null
    }
)
