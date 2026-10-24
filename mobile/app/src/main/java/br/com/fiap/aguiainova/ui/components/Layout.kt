package br.com.fiap.aguiainova.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.CircularProgressIndicator
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import br.com.fiap.aguiainova.domain.model.Perfil
import br.com.fiap.aguiainova.navigation.Rotas
import br.com.fiap.aguiainova.navigation.irParaAba
import br.com.fiap.aguiainova.ui.common.Estado
import br.com.fiap.aguiainova.ui.theme.AguiaCores

/**
 * Estrutura padrao de toda tela: cabecalho (titulo, subtitulo, voltar, acoes), barra inferior do perfil, botao
 * flutuante e mensagens curtas (snackbar). O [conteudo] recebe o espaco restante.
 */
@Composable
fun TelaBase(
    titulo: String,
    subtitulo: String? = null,
    navController: NavController? = null,
    perfil: Perfil? = null,
    aoVoltar: (() -> Unit)? = null,
    acoes: @Composable RowScope.() -> Unit = {},
    botaoFlutuante: @Composable () -> Unit = {},
    aviso: String? = null,
    aoAvisoLido: () -> Unit = {},
    conteudo: @Composable (Modifier) -> Unit
) {
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(aviso) {
        if (aviso != null) {
            snackbar.showSnackbar(aviso)
            aoAvisoLido()
        }
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = { if (navController != null && perfil != null) BarraInferior(perfil, navController) },
        floatingActionButton = botaoFlutuante
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Cabecalho(titulo, subtitulo, aoVoltar, acoes)
            conteudo(Modifier.weight(1f).fillMaxWidth())
        }
    }
}

@Composable
private fun Cabecalho(
    titulo: String,
    subtitulo: String?,
    aoVoltar: (() -> Unit)?,
    acoes: @Composable RowScope.() -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (aoVoltar != null) {
            IconButton(onClick = aoVoltar) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
            }
        }
        Column(Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            if (subtitulo != null) {
                Text(subtitulo, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        acoes()
    }
}

/** Mostra "carregando", o erro (com botao para tentar de novo) ou o conteudo pronto. */
@Composable
fun <T> ConteudoDoEstado(
    estado: Estado<T>,
    aoTentarNovamente: () -> Unit,
    modifier: Modifier = Modifier,
    pronto: @Composable (T) -> Unit
) {
    when (estado) {
        Estado.Carregando -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is Estado.Erro -> Column(
            modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(estado.mensagem, color = AguiaCores.Erro)
            Spacer(Modifier.height(12.dp))
            Button(onClick = aoTentarNovamente) { Text("Tentar novamente") }
        }
        is Estado.Pronto -> Box(modifier) { pronto(estado.dados) }
    }
}

@Composable
fun MensagemVazia(texto: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(texto, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private data class ItemBarra(val rota: String, val rotulo: String, val icone: ImageVector)

/**
 * Abas de cada perfil: a barra e a mesma; o que muda e a lista de itens (dados, nao codigo duplicado). A mesma tela de
 * estrategias aparece como "Estratégia" (operador e lideranca) ou "Direção" (gestor, que ali tambem tem o assistente).
 */
private fun itensDe(perfil: Perfil): List<ItemBarra> {
    val inicio = ItemBarra(Rotas.homeDe(perfil), "Início", Icons.Default.Home)
    val ideias = ItemBarra(Rotas.IDEIAS, "Ideias", Icons.Default.Lightbulb)
    val estrategia = ItemBarra(Rotas.ESTRATEGIAS, "Estratégia", Icons.Default.Flag)
    val direcao = ItemBarra(Rotas.ESTRATEGIAS, "Direção", Icons.Default.Explore)
    val projetos = ItemBarra(Rotas.PROJETOS, "Projetos", Icons.Default.Work)
    val painel = ItemBarra(Rotas.DASHBOARD, "Painel", Icons.Default.BarChart)
    val perfilAba = ItemBarra(Rotas.PERFIL, "Perfil", Icons.Default.Person)
    return when (perfil) {
        Perfil.OPERADOR -> listOf(inicio, ideias, estrategia, perfilAba)
        Perfil.GESTOR -> listOf(inicio, ideias, projetos, direcao, perfilAba)
        Perfil.LIDERANCA -> listOf(inicio, estrategia, painel, projetos, perfilAba)
    }
}

@Composable
fun BarraInferior(perfil: Perfil, navController: NavController) {
    // A rota da tela de estrategias carrega um argumento opcional ("estrategias?aba=..."): compara so o caminho.
    val rotaAtual = navController.currentBackStackEntryAsState().value?.destination?.route?.substringBefore('?')
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        itensDe(perfil).forEach { item ->
            NavigationBarItem(
                selected = rotaAtual == item.rota,
                onClick = { navController.irParaAba(item.rota, perfil) },
                icon = { Icon(item.icone, contentDescription = item.rotulo) },
                label = { Text(item.rotulo) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AguiaCores.Primaria,
                    selectedTextColor = AguiaCores.Primaria,
                    indicatorColor = MaterialTheme.colorScheme.background,
                    unselectedIconColor = AguiaCores.TextoSecundario,
                    unselectedTextColor = AguiaCores.TextoSecundario
                )
            )
        }
    }
}
