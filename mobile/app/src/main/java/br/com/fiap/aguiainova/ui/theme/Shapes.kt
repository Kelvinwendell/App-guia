package br.com.fiap.aguiainova.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Formas padrao do Material3 usadas pelo app (Button, TextField, Dialog, etc.). Componentes com raio
 * proprio ja definido (CartaoAguia com 20.dp, Selo totalmente arredondado) continuam como estao.
 */
val AguiaShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
