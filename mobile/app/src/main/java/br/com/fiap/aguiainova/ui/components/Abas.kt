package br.com.fiap.aguiainova.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.fiap.aguiainova.ui.theme.AguiaCores

/**
 * Abas de uma tela: dividem o conteudo da MESMA tela em partes (ao contrario da barra inferior, que troca de tela).
 * Com ate tres abas elas dividem a largura; com mais, a fileira rola. Quem usa decide o que mostrar para cada aba.
 */
@Composable
fun AbasDaTela(
    rotulos: List<String>,
    selecionada: Int,
    aoSelecionar: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val indice = selecionada.coerceIn(0, rotulos.lastIndex)
    val abas: @Composable () -> Unit = {
        rotulos.forEachIndexed { posicao, rotulo ->
            Tab(
                selected = posicao == indice,
                onClick = { aoSelecionar(posicao) },
                text = {
                    Text(
                        rotulo,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = if (posicao == indice) FontWeight.Bold else FontWeight.Medium
                    )
                },
                selectedContentColor = AguiaCores.Primaria,
                unselectedContentColor = AguiaCores.TextoSecundario
            )
        }
    }
    if (rotulos.size > ABAS_SEM_ROLAGEM) {
        ScrollableTabRow(
            selectedTabIndex = indice,
            modifier = modifier.padding(bottom = 4.dp),
            containerColor = Color.Transparent,
            contentColor = AguiaCores.Primaria,
            edgePadding = 0.dp,
            tabs = abas
        )
    } else {
        TabRow(
            selectedTabIndex = indice,
            modifier = modifier.padding(bottom = 4.dp),
            containerColor = Color.Transparent,
            contentColor = AguiaCores.Primaria,
            tabs = abas
        )
    }
}

private const val ABAS_SEM_ROLAGEM = 3
