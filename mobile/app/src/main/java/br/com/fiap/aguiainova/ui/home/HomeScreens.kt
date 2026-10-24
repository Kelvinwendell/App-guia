package br.com.fiap.aguiainova.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.MaterialTheme
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
import br.com.fiap.aguiainova.ui.components.CartaoAcao
import br.com.fiap.aguiainova.ui.components.CartaoIndicador
import br.com.fiap.aguiainova.ui.components.ConteudoDoEstado
import br.com.fiap.aguiainova.ui.components.TelaBase
import br.com.fiap.aguiainova.ui.components.TituloSecao
import br.com.fiap.aguiainova.ui.theme.AguiaCores
import br.com.fiap.aguiainova.util.Formatadores

/** Estrutura comum das telas iniciais: carrega o resumo do perfil e entrega o conteudo pronto. */
@Composable
private fun TelaInicial(
    perfil: Perfil,
    navController: NavController,
    titulo: String,
    subtitulo: String,
    conteudo: @Composable (ResumoHome) -> Unit
) {
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
                conteudo(resumo)
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun HomeOperadorScreen(navController: NavController) {
    TelaInicial(Perfil.OPERADOR, navController, "Painel do Operador", "Registre ideias e acompanhe o resultado.") { resumo ->
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CartaoIndicador("Minhas ideias", resumo.ideiasTotal.toString(), AguiaCores.Primaria, Modifier.weight(1f))
            CartaoIndicador("Aprovadas", resumo.ideiasAprovadas.toString(), AguiaCores.Sucesso, Modifier.weight(1f))
        }
        TituloSecao("Ações rápidas")
        CartaoAcao("Minhas ideias", "Cadastre e acompanhe o status", Icons.Default.Lightbulb) {
            navController.navigate(Rotas.IDEIAS)
        }
        CartaoAcao("Estratégia da empresa", "Veja o que a empresa quer alcançar e alinhe suas ideias", Icons.Default.Flag) {
            navController.navigate(Rotas.ESTRATEGIAS)
        }
    }
}

@Composable
fun HomeGestorScreen(navController: NavController) {
    TelaInicial(Perfil.GESTOR, navController, "Painel do Gestor", "Gerencie ideias, projetos e acompanhe resultados.") { resumo ->
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CartaoIndicador("Ideias pendentes", resumo.ideiasPendentes.toString(), AguiaCores.Primaria, Modifier.weight(1f))
            CartaoIndicador("Projetos ativos", resumo.projetosAtivos.toString(), AguiaCores.Sucesso, Modifier.weight(1f))
        }
        CartaoIndicador("Retorno parcial", Formatadores.moeda(resumo.retornoParcial), AguiaCores.Alerta)
        TituloSecao("Ações rápidas")
        CartaoAcao("Aprovar ideias", "Analise, priorize e aprove ideias enviadas", Icons.Default.Lightbulb) {
            navController.navigate(Rotas.IDEIAS)
        }
        CartaoAcao("Assistente de IA", "Converse para escolher ideias e montar projetos", Icons.Default.AutoAwesome) {
            navController.navigate(Rotas.estrategias(Rotas.ABA_ASSISTENTE))
        }
        CartaoAcao("Criar projeto", "Cadastre novas iniciativas", Icons.Default.Work) {
            navController.navigate(Rotas.PROJETO_NOVO)
        }
        CartaoAcao("Atualizar progresso", "Acompanhe indicadores e entregas", Icons.Default.Task) {
            navController.navigate(Rotas.PROJETOS)
        }
        CartaoAcao("Saúde das estratégias", "Veja quais estão em dia, em risco ou paradas", Icons.Default.Flag) {
            navController.navigate(Rotas.ESTRATEGIAS)
        }
    }
}

@Composable
fun HomeLiderancaScreen(navController: NavController) {
    TelaInicial(Perfil.LIDERANCA, navController, "Painel da Liderança", "Visão executiva da inovação.") { resumo ->
        val painel = resumo.dashboard
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CartaoIndicador("ROI", Formatadores.percentual(painel?.roiPercentual), AguiaCores.Sucesso, Modifier.weight(1f))
            CartaoIndicador("Custos", Formatadores.percentualComSinal(painel?.reducaoCustosMedia?.unaryMinus()), AguiaCores.Alerta, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CartaoIndicador("Produtividade", Formatadores.percentualComSinal(painel?.ganhoProdutividadeMedio), AguiaCores.Primaria, Modifier.weight(1f))
            CartaoIndicador("Projetos", (painel?.totalProjetos ?: 0).toString(), MaterialTheme.colorScheme.onSurfaceVariant, Modifier.weight(1f))
        }
        TituloSecao("Ações rápidas")
        CartaoAcao("Painel executivo", "ROI, lucro, prazos e insights de IA", Icons.Default.BarChart) {
            navController.navigate(Rotas.DASHBOARD)
        }
        CartaoAcao("Estratégia", "Crie com a IA, acompanhe a saúde e revise as diretrizes", Icons.Default.Flag) {
            navController.navigate(Rotas.ESTRATEGIAS)
        }
        CartaoAcao("Projetos estratégicos", "Acompanhe etapa, status, investimento e retorno", Icons.Default.Work) {
            navController.navigate(Rotas.PROJETOS)
        }
    }
}
