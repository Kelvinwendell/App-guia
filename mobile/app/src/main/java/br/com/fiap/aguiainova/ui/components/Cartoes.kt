package br.com.fiap.aguiainova.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import br.com.fiap.aguiainova.ui.theme.AguiaCores

@Composable
fun CartaoAguia(
    modifier: Modifier = Modifier,
    aoClicar: (() -> Unit)? = null,
    conteudo: @Composable () -> Unit
) {
    val cores = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    val forma = MaterialTheme.shapes.medium
    if (aoClicar != null) {
        Card(onClick = aoClicar, modifier = modifier.fillMaxWidth(), colors = cores, shape = forma) { conteudo() }
    } else {
        Card(modifier = modifier.fillMaxWidth(), colors = cores, shape = forma) { conteudo() }
    }
}

/** Indicador numerico (titulo + valor) usado nas telas iniciais e no dashboard. Unico no app (havia tres copias). */
@Composable
fun CartaoIndicador(
    titulo: String,
    valor: String,
    cor: Color,
    modifier: Modifier = Modifier
) {
    CartaoAguia(modifier) {
        Column(Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(cor))
                Text(titulo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            Text(valor, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Cartao clicavel com icone, titulo e subtitulo (acoes rapidas das telas iniciais). [destaque] marca a
 * acao primaria da tela (PRD O2: exatamente uma por home) com fundo preenchido em vez de contorno sutil.
 */
@Composable
fun CartaoAcao(
    titulo: String,
    subtitulo: String,
    icone: ImageVector,
    destaque: Boolean = false,
    aoClicar: () -> Unit
) {
    Card(
        onClick = aoClicar,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (destaque) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
        ),
        border = if (destaque) BorderStroke(1.dp, AguiaCores.Primaria.copy(alpha = 0.25f)) else null
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(AguiaCores.Primaria.copy(alpha = if (destaque) 1f else 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icone, contentDescription = null, tint = if (destaque) AguiaCores.Fundo else AguiaCores.Primaria)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium)
                Text(subtitulo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Etiqueta colorida (status, prioridade, origem da nota de IA...). */
@Composable
fun Selo(texto: String, cor: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = cor.copy(alpha = 0.16f)
    ) {
        Text(
            texto,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            color = cor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun LinhaInfo(rotulo: String, valor: String, modifier: Modifier = Modifier, corValor: Color = Color.Unspecified) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(rotulo, color = MaterialTheme.colorScheme.onSurfaceVariant)
        // O valor ocupa o resto da linha, alinhado a direita; se for longo, quebra em vez de encostar no rotulo.
        Text(valor, fontWeight = FontWeight.Medium, color = corValor, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
    }
}

@Composable
fun BarraProgresso(progresso: Int, modifier: Modifier = Modifier) {
    LinearProgressIndicator(
        progress = { (progresso / 100f).coerceIn(0f, 1f) },
        modifier = modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
        color = AguiaCores.Primaria,
        trackColor = MaterialTheme.colorScheme.outline
    )
}

@Composable
fun TituloSecao(texto: String, modifier: Modifier = Modifier) {
    Text(
        texto,
        modifier = modifier.padding(top = 12.dp, bottom = 2.dp),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}
