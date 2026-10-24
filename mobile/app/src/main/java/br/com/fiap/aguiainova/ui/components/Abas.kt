package br.com.fiap.aguiainova.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.fiap.aguiainova.ui.theme.AguiaCores

/**
 * Abas de uma tela: dividem o conteudo da MESMA tela em partes (ao contrario da barra inferior, que troca de tela).
 * Com ate tres abas elas dividem a largura; com mais, a fileira rola. Quando sobra aba fora da tela (comum com fonte
 * ampliada) a borda direita ganha um degrade com seta: antes a quarta aba sumia sem nenhuma pista de que existia.
 */
@Composable
fun AbasDaTela(
    rotulos: List<String>,
    selecionada: Int,
    aoSelecionar: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val indice = selecionada.coerceIn(0, rotulos.lastIndex)
    if (rotulos.size <= ABAS_SEM_ROLAGEM) {
        TabRow(
            selectedTabIndex = indice,
            modifier = modifier.padding(bottom = 4.dp),
            containerColor = Color.Transparent,
            contentColor = AguiaCores.Primaria
        ) {
            // A TabRow ja desenha o indicador animado; o sublinhado proprio e so da fileira rolavel.
            rotulos.forEachIndexed { posicao, rotulo -> Aba(rotulo, posicao == indice, { aoSelecionar(posicao) }, sublinhar = false) }
        }
    } else {
        AbasRolaveis(rotulos, indice, aoSelecionar, modifier.padding(bottom = 4.dp))
    }
}

@Composable
private fun AbasRolaveis(rotulos: List<String>, indice: Int, aoSelecionar: (Int) -> Unit, modifier: Modifier) {
    val rolagem = rememberScrollState()
    val limites = remember { mutableStateMapOf<Int, IntRange>() }
    var larguraVisivel by remember { mutableIntStateOf(0) }
    // A aba escolhida (inclusive vinda de outra tela, ex.: "estrategias?aba=historico") fica a vista, rolando so o
    // necessario: alinhar sempre a esquerda escondia as abas anteriores.
    LaunchedEffect(indice, limites[indice], larguraVisivel) {
        val aba = limites[indice] ?: return@LaunchedEffect
        val alvo = when {
            aba.first < rolagem.value -> aba.first
            aba.last > rolagem.value + larguraVisivel -> aba.last - larguraVisivel
            else -> return@LaunchedEffect
        }
        rolagem.animateScrollTo(alvo)
    }
    Box(modifier.onGloballyPositioned { larguraVisivel = it.size.width }) {
        Column {
            Row(Modifier.horizontalScroll(rolagem)) {
                rotulos.forEachIndexed { posicao, rotulo ->
                    Aba(
                        rotulo, posicao == indice, { aoSelecionar(posicao) },
                        Modifier.widthIn(min = LARGURA_MINIMA_ABA).onGloballyPositioned {
                            val inicio = it.positionInParent().x.toInt()
                            limites[posicao] = inicio..(inicio + it.size.width)
                        }
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        }
        PistaDeRolagem(rolagem.canScrollBackward, paraADireita = false, Modifier.align(Alignment.CenterStart))
        PistaDeRolagem(rolagem.canScrollForward, paraADireita = true, Modifier.align(Alignment.CenterEnd))
    }
}

/** Degrade com seta na borda que tem aba escondida; some quando nao ha mais nada para aquele lado. */
@Composable
private fun PistaDeRolagem(visivel: Boolean, paraADireita: Boolean, modifier: Modifier) {
    val opacidade by animateFloatAsState(if (visivel) 1f else 0f, label = "pista")
    val fundo = MaterialTheme.colorScheme.background
    val cores = if (paraADireita) listOf(Color.Transparent, fundo) else listOf(fundo, Color.Transparent)
    Box(
        modifier.alpha(opacidade).width(40.dp).height(48.dp).background(Brush.horizontalGradient(cores)),
        contentAlignment = if (paraADireita) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Icon(
            if (paraADireita) Icons.AutoMirrored.Filled.KeyboardArrowRight else Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun Aba(
    rotulo: String,
    selecionada: Boolean,
    aoClicar: () -> Unit,
    modifier: Modifier = Modifier,
    sublinhar: Boolean = true
) {
    val sublinhado = AguiaCores.Primaria
    Tab(
        selected = selecionada,
        onClick = aoClicar,
        modifier = if (selecionada && sublinhar) modifier.drawBehind {
            val espessura = 3.dp.toPx()
            drawLine(sublinhado, Offset(0f, size.height - espessura / 2), Offset(size.width, size.height - espessura / 2), espessura)
        } else modifier,
        text = {
            Text(
                rotulo,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if (selecionada) FontWeight.Bold else FontWeight.Medium
            )
        },
        selectedContentColor = AguiaCores.Primaria,
        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

private const val ABAS_SEM_ROLAGEM = 3
private val LARGURA_MINIMA_ABA = 90.dp
