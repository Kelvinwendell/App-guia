package br.com.fiap.aguiainova.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.fiap.aguiainova.ui.theme.AguiaCores
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import br.com.fiap.aguiainova.util.Formatadores
import kotlin.math.abs

data class ItemGrafico(val rotulo: String, val valor: Double, val cor: Color? = null)

/**
 * Barras horizontais. Convencao: o comprimento e a magnitude (|valor| / maior |valor|); o sinal fica no texto
 * ("-12,0%") e na cor vermelha, entao um negativo nunca parece ganho. Cada linha e lida como "rotulo: valor".
 */
@Composable
fun GraficoBarras(
    itens: List<ItemGrafico>,
    formatarValor: (Double) -> String,
    modifier: Modifier = Modifier
) {
    val maximo = itens.maxOfOrNull { abs(it.valor) }?.takeIf { it > 0 } ?: 1.0
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        itens.forEach { item ->
            val cor = item.cor ?: if (item.valor < 0) AguiaCores.Erro else AguiaCores.Primaria
            Column(Modifier.semantics(mergeDescendants = true) {}) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(item.rotulo, modifier = Modifier.weight(1f), maxLines = 1, style = MaterialTheme.typography.bodyMedium)
                    Text(formatarValor(item.valor), color = cor, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(4.dp))
                Box(Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(MaterialTheme.colorScheme.outline)) {
                    Box(
                        Modifier
                            .fillMaxWidth(fracaoDaBarra(item.valor, maximo))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(5.dp))
                            .background(cor)
                    )
                }
            }
        }
    }
}

/**
 * Colunas verticais (ex.: retorno por mes), com o rotulo embaixo de cada uma. Mesma convencao de [GraficoBarras]:
 * altura pela magnitude, negativo em vermelho. Como a coluna nao mostra o numero, cada uma descreve "rotulo:
 * valor" para o leitor de tela. Acima de [COLUNAS_SEM_ROLAGEM] itens as colunas ganham largura fixa e o grafico rola
 * na horizontal, dentro do proprio cartao, em vez de espremer 24 meses em fiapos com rotulos sobrepostos.
 */
@Composable
fun GraficoColunas(
    itens: List<ItemGrafico>,
    modifier: Modifier = Modifier,
    altura: Int = 150,
    formatarValor: (Double) -> String = Formatadores::moeda
) {
    val maximo = itens.maxOfOrNull { abs(it.valor) }?.takeIf { it > 0 } ?: 1.0
    val rola = itens.size > COLUNAS_SEM_ROLAGEM
    Row(
        (if (rola) modifier.fillMaxWidth().horizontalScroll(rememberScrollState()) else modifier.fillMaxWidth())
            .height((altura + ALTURA_ROTULO).dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        itens.forEach { item ->
            val cor = item.cor ?: if (item.valor < 0) AguiaCores.Erro else AguiaCores.Primaria
            val largura = if (rola) Modifier.width(LARGURA_COLUNA_FIXA.dp) else Modifier.weight(1f)
            Column(
                largura.semantics(mergeDescendants = true) {
                    contentDescription = "${item.rotulo}: ${formatarValor(item.valor)}"
                },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(Modifier.height(altura.dp).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                    Box(
                        Modifier
                            .fillMaxWidth(0.7f)
                            .fillMaxHeight(fracaoDaBarra(item.valor, maximo))
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(cor)
                    )
                }
                Text(
                    item.rotulo,
                    modifier = Modifier.padding(top = 4.dp).clearAndSetSemantics {},
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

/** Fracao visivel da barra: minimo garantido so para valores reais e diferentes de zero,
 * senao um valor zero fica com barra visivel e parece que ha dado onde nao ha. */
internal fun fracaoDaBarra(valor: Double, maximo: Double): Float =
    if (valor == 0.0) 0f else (abs(valor) / maximo).toFloat().coerceIn(MENOR_FRACAO, 1f)

private const val MENOR_FRACAO = 0.02f
private const val ALTURA_ROTULO = 24
internal const val COLUNAS_SEM_ROLAGEM = 8
private const val LARGURA_COLUNA_FIXA = 44
