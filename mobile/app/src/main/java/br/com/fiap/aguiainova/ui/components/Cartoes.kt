package br.com.fiap.aguiainova.ui.components

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
    val cores = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val forma = RoundedCornerShape(20.dp)
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
        Column(Modifier.padding(18.dp)) {
            Box(Modifier.size(14.dp).clip(CircleShape).background(cor))
            Spacer(Modifier.height(12.dp))
            Text(titulo, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(valor, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

/** Cartao clicavel com icone, titulo e subtitulo (acoes rapidas das telas iniciais). */
@Composable
fun CartaoAcao(
    titulo: String,
    subtitulo: String,
    icone: ImageVector,
    aoClicar: () -> Unit
) {
    CartaoAguia(aoClicar = aoClicar) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(AguiaCores.Primaria),
                contentAlignment = Alignment.Center
            ) {
                Icon(icone, contentDescription = null, tint = Color.White)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(subtitulo, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
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
        modifier = modifier.padding(top = 20.dp, bottom = 10.dp),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}
