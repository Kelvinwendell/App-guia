package br.com.fiap.aguiainova.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val EsquemaEscuro = darkColorScheme(
    primary = AguiaCores.Primaria,
    onPrimary = AguiaCores.Fundo,
    primaryContainer = AguiaCores.SuperficieElevada,
    onPrimaryContainer = AguiaCores.Primaria,
    secondary = AguiaCores.Sucesso,
    onSecondary = Color.Black,
    tertiary = AguiaCores.Alerta,
    background = AguiaCores.Fundo,
    onBackground = Color.White,
    surface = AguiaCores.Superficie,
    surfaceContainerLowest = AguiaCores.Fundo,
    surfaceContainerLow = AguiaCores.Superficie,
    surfaceContainer = AguiaCores.Superficie,
    surfaceContainerHigh = AguiaCores.SuperficieElevada,
    surfaceContainerHighest = AguiaCores.SuperficieElevada,
    onSurface = Color.White,
    surfaceVariant = AguiaCores.SuperficieElevada,
    onSurfaceVariant = AguiaCores.TextoSecundario,
    outline = AguiaCores.Borda,
    outlineVariant = AguiaCores.Borda.copy(alpha = 0.55f),
    error = AguiaCores.Erro,
    onError = AguiaCores.Fundo
)

@Composable
fun AguiaTheme(conteudo: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaEscuro,
        typography = Typography,
        shapes = Shapes(
            extraSmall = RoundedCornerShape(4.dp),
            small = RoundedCornerShape(8.dp),
            medium = RoundedCornerShape(12.dp),
            large = RoundedCornerShape(16.dp),
            extraLarge = RoundedCornerShape(28.dp)
        ),
        content = conteudo
    )
}
