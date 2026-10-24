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
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Work
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
                .padding(horizontal = 20.dp)
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
            .padding(top = 24.dp, bottom = 20.dp),
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
                Text(
                    subtitulo,
                    modifier = Modifier.padding(top = 6.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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

/** [acaoTexto]/[aoAcao] so aparecem juntos: uma acao opcional (ex.: "Criar a primeira ideia"). */
@Composable
fun MensagemVazia(
    texto: String,
    modifier: Modifier = Modifier,
    acaoTexto: String? = null,
    aoAcao: (() -> Unit)? = null
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(texto, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (acaoTexto != null && aoAcao != null) {
                Spacer(Modifier.height(12.dp))
                Button(onClick = aoAcao) { Text(acaoTexto) }
            }
        }
    }
}

private data class ItemBarra(val rota: String, val rotulo: String, val icone: ImageVector)

/**
 * Abas de cada perfil: a barra e a mesma; o que muda e a lista de itens (dados, nao codigo duplicado). A mesma tela de
 * estrategias aparece como "Estratégia" (operador e lideranca) ou "Direção" (gestor, que ali tambem tem o assistente).
 */
private fun itensDe(perfil: Perfil): List<ItemBarra> {
    val inicio = ItemBarra(Rotas.homeDe(perfil), "Início", Icons.Outlined.Home)
    val ideias = ItemBarra(Rotas.IDEIAS, "Ideias", Icons.Outlined.Lightbulb)
    val estrategia = ItemBarra(Rotas.ESTRATEGIAS, "Estratégia", Icons.Outlined.Flag)
    val direcao = ItemBarra(Rotas.ESTRATEGIAS, "Direção", Icons.Outlined.Explore)
    val projetos = ItemBarra(Rotas.PROJETOS, "Projetos", Icons.Outlined.Work)
    val painel = ItemBarra(Rotas.DASHBOARD, "Painel", Icons.Outlined.BarChart)
    val perfilAba = ItemBarra(Rotas.PERFIL, "Perfil", Icons.Outlined.Person)
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
    NavigationBar(containerColor = MaterialTheme.colorScheme.background, tonalElevation = 0.dp) {
        itensDe(perfil).forEach { item ->
            NavigationBarItem(
                selected = rotaAtual == item.rota,
                onClick = { navController.irParaAba(item.rota, perfil) },
                icon = { Icon(item.icone, contentDescription = item.rotulo) },
                label = { Text(item.rotulo, style = MaterialTheme.typography.labelSmall, maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AguiaCores.Primaria,
                    selectedTextColor = AguiaCores.Primaria,
                    indicatorColor = AguiaCores.Primaria.copy(alpha = 0.12f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
