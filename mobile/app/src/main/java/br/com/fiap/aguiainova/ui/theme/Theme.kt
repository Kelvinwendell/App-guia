package br.com.fiap.aguiainova.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaEscuro = darkColorScheme(
    primary = AguiaCores.Primaria,
    onPrimary = Color.White,
    secondary = AguiaCores.Sucesso,
    onSecondary = Color.Black,
    tertiary = AguiaCores.Alerta,
    background = NeutrosEscuro.Fundo,
    onBackground = Color.White,
    surface = NeutrosEscuro.Superficie,
    onSurface = Color.White,
    surfaceVariant = NeutrosEscuro.SuperficieElevada,
    onSurfaceVariant = NeutrosEscuro.TextoSecundario,
    outline = NeutrosEscuro.Borda,
    error = AguiaCores.Erro,
    onError = Color.White
)

private val EsquemaClaro = lightColorScheme(
    primary = AguiaCores.Primaria,
    onPrimary = Color.White,
    secondary = AguiaCores.Sucesso,
    onSecondary = Color.White,
    tertiary = AguiaCores.Alerta,
    background = NeutrosClaro.Fundo,
    onBackground = Color(0xFF14161C),
    surface = NeutrosClaro.Superficie,
    onSurface = Color(0xFF14161C),
    surfaceVariant = NeutrosClaro.SuperficieElevada,
    onSurfaceVariant = NeutrosClaro.TextoSecundario,
    outline = NeutrosClaro.Borda,
    error = AguiaCores.Erro,
    onError = Color.White
)

/**
 * Tema do app: segue o tema do sistema (claro/escuro) por padrao. [escuroForcado] permite travar um
 * dos dois modos quando necessario (ex.: previews).
 */
@Composable
fun AguiaTheme(
    escuroForcado: Boolean? = null,
    conteudo: @Composable () -> Unit
) {
    val usarEscuro = escuroForcado ?: isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (usarEscuro) EsquemaEscuro else EsquemaClaro,
        typography = Typography,
        shapes = AguiaShapes,
        content = conteudo
    )
}
