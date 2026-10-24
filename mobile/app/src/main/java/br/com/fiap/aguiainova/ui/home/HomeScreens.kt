package br.com.fiap.aguiainova.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import br.com.fiap.aguiainova.domain.model.Perfil
import br.com.fiap.aguiainova.navigation.Rotas
import br.com.fiap.aguiainova.ui.common.AoRetomar
import br.com.fiap.aguiainova.ui.common.viewModelDoApp
import br.com.fiap.aguiainova.ui.components.ConteudoDoEstado
import br.com.fiap.aguiainova.ui.components.TelaBase

/** Estrutura comum das telas iniciais: carrega o resumo do perfil, liga callbacks a Rotas e entrega o HomeConteudo. */
@Composable
private fun TelaInicial(perfil: Perfil, navController: NavController, titulo: String, subtitulo: String) {
    val vm = viewModelDoApp(chave = "home-${perfil.name}") {
        HomeViewModel(perfil, it.ideias, it.projetos, it.dashboard)
    }
    val estado by vm.estado.collectAsStateWithLifecycle()
    val aviso by vm.aviso.collectAsStateWithLifecycle()
    AoRetomar(vm::carregar)

    TelaBase(
        titulo = titulo,
        subtitulo = subtitulo,
        navController = navController,
        perfil = perfil,
        aviso = aviso,
        aoAvisoLido = vm::avisoLido
    ) { modificador ->
        ConteudoDoEstado(estado, aoTentarNovamente = vm::carregar, modifier = modificador) { resumo ->
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                HomeConteudo(
                    perfil = perfil,
                    resumo = resumo,
                    aoAbrirIdeias = { navController.navigate(Rotas.IDEIAS) },
                    aoAbrirEstrategias = { navController.navigate(Rotas.ESTRATEGIAS) },
                    aoAbrirProjetos = { navController.navigate(Rotas.PROJETOS) },
                    aoCriarProjeto = { navController.navigate(Rotas.PROJETO_NOVO) },
                    aoAbrirAssistente = { navController.navigate(Rotas.estrategias(Rotas.ABA_ASSISTENTE)) },
                    aoAbrirDashboard = { navController.navigate(Rotas.DASHBOARD) }
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun HomeOperadorScreen(navController: NavController) {
    TelaInicial(Perfil.OPERADOR, navController, "Painel do Operador", "Registre ideias e acompanhe o resultado.")
}

@Composable
fun HomeGestorScreen(navController: NavController) {
    TelaInicial(Perfil.GESTOR, navController, "Painel do Gestor", "Gerencie ideias, projetos e acompanhe resultados.")
}

@Composable
fun HomeLiderancaScreen(navController: NavController) {
    TelaInicial(Perfil.LIDERANCA, navController, "Painel da Liderança", "Visão executiva da inovação.")
}
