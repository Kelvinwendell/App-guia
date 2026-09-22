package br.com.fiap.aguiainova.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaEscuro = darkColorScheme(
    primary = AguiaCores.Primaria,
    onPrimary = Color.White,
    secondary = AguiaCores.Sucesso,
    onSecondary = Color.Black,
    tertiary = AguiaCores.Alerta,
    background = AguiaCores.Fundo,
    onBackground = Color.White,
    surface = AguiaCores.Superficie,
    onSurface = Color.White,
    surfaceVariant = AguiaCores.SuperficieElevada,
    onSurfaceVariant = AguiaCores.TextoSecundario,
    outline = AguiaCores.Borda,
    error = AguiaCores.Erro,
    onError = Color.White
)

@Composable
fun AguiaTheme(conteudo: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaEscuro,
        typography = Typography,
        content = conteudo
    )
}
