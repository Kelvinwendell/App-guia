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
import kotlin.math.abs

data class ItemGrafico(val rotulo: String, val valor: Double, val cor: Color? = null)

/** Barras horizontais proporcionais ao maior valor; valores negativos ficam vermelhos. */
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
            Column {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(item.rotulo, modifier = Modifier.weight(1f), maxLines = 1, style = MaterialTheme.typography.bodyMedium)
                    Text(formatarValor(item.valor), color = cor, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(4.dp))
                Box(Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(AguiaCores.Borda)) {
                    Box(
                        Modifier
                            .fillMaxWidth((abs(item.valor) / maximo).toFloat().coerceIn(MENOR_FRACAO, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(5.dp))
                            .background(cor)
                    )
                }
            }
        }
    }
}

/** Colunas verticais (ex.: retorno por mes), com o rotulo embaixo de cada uma. */
@Composable
fun GraficoColunas(
    itens: List<ItemGrafico>,
    modifier: Modifier = Modifier,
    altura: Int = 150
) {
    val maximo = itens.maxOfOrNull { abs(it.valor) }?.takeIf { it > 0 } ?: 1.0
    Row(
        modifier.fillMaxWidth().height((altura + ALTURA_ROTULO).dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        itens.forEach { item ->
            val cor = item.cor ?: if (item.valor < 0) AguiaCores.Erro else AguiaCores.Primaria
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.height(altura.dp).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                    Box(
                        Modifier
                            .fillMaxWidth(0.7f)
                            .fillMaxHeight((abs(item.valor) / maximo).toFloat().coerceIn(MENOR_FRACAO, 1f))
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(cor)
                    )
                }
                Text(
                    item.rotulo,
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

private const val MENOR_FRACAO = 0.02f
private const val ALTURA_ROTULO = 24
