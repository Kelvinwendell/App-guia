package br.com.fiap.aguiainova.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Escala de espacamento do app (docs/redesign/specs/001-fundacoes-visuais.md). Usar estes tokens em
 * componentes/telas novas em vez de ".dp" solto — telas existentes ainda usam valores soltos; migrar
 * e um refactor incremental, feito junto com a spec de cada tela (RD-002 em diante).
 */
object AguiaEspacamentos {
    val Minimo: Dp = 4.dp
    val Pequeno: Dp = 8.dp
    val Medio: Dp = 12.dp
    val Conteudo: Dp = 16.dp
    val Tela: Dp = 20.dp
    val Secao: Dp = 24.dp
}
