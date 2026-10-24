package br.com.fiap.aguiainova.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Escala de espacamento do app. Usar estes tokens em componentes/telas novas em vez de ".dp" solto —
 * mantem ritmo visual consistente e permite ajustar a escala inteira num lugar so. Telas existentes
 * ainda usam valores soltos; migrar para os tokens e um refactor incremental, nao bloqueante.
 */
object AguiaEspacamento {
    val ExtraPequeno: Dp = 4.dp
    val Pequeno: Dp = 8.dp
    val Medio: Dp = 16.dp
    val Grande: Dp = 24.dp
    val ExtraGrande: Dp = 32.dp
}
