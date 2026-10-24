package br.com.fiap.aguiainova.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

/**
 * Linha de selos que quebra para a linha de baixo quando nao cabe (360dp, fonte 150%, nome de campanha longo).
 * Layout proprio de proposito: o `FlowRow` do foundation compila, mas da `NoSuchMethodError` nos testes com o BOM
 * atual (ver docs/redesign/PENDENTE-RD-007.md).
 */
@Composable
fun LinhaQuebravel(modifier: Modifier = Modifier, espaco: Dp = 8.dp, conteudo: @Composable () -> Unit) {
    Layout(conteudo, modifier) { medidas, restricoes ->
        val vao = espaco.roundToPx()
        val largura = restricoes.maxWidth
        val itens = medidas.map { it.measure(restricoes.copy(minWidth = 0, minHeight = 0)) }
        var x = 0
        var y = 0
        var alturaDaLinha = 0
        var larguraUsada = 0
        val posicoes = itens.map { item ->
            if (x > 0 && x + item.width > largura) {
                x = 0
                y += alturaDaLinha + vao
                alturaDaLinha = 0
            }
            val posicao = IntOffset(x, y)
            larguraUsada = maxOf(larguraUsada, x + item.width)
            x += item.width + vao
            alturaDaLinha = maxOf(alturaDaLinha, item.height)
            posicao
        }
        layout(
            larguraUsada.coerceIn(restricoes.minWidth, largura),
            (y + alturaDaLinha).coerceIn(restricoes.minHeight, restricoes.maxHeight)
        ) {
            itens.forEachIndexed { i, item -> item.place(posicoes[i]) }
        }
    }
}

/**
 * Dois ou mais campos lado a lado quando ha espaco e um embaixo do outro em tela estreita ou fonte ampliada. A largura
 * e dividida pela escala da fonte: 360dp com fonte 150% conta como 240dp. Cada item recebe o [Modifier] de largura.
 */
@Composable
fun LadoALadoOuEmpilhado(
    modifier: Modifier = Modifier,
    larguraMinima: Dp = 320.dp,
    espaco: Dp = 8.dp,
    conteudo: @Composable (Modifier) -> Unit
) {
    val escalaDaFonte = LocalDensity.current.fontScale
    BoxWithConstraints(modifier.fillMaxWidth()) {
        if (maxWidth / escalaDaFonte < larguraMinima) {
            Column(verticalArrangement = Arrangement.spacedBy(espaco)) { conteudo(Modifier.fillMaxWidth()) }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(espaco)) { conteudo(Modifier.weight(1f)) }
        }
    }
}
