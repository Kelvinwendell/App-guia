package br.com.fiap.aguiainova.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Lista sem itens: diz o que e a lista e, quando ha algo a fazer, oferece o proximo passo (antes era so uma frase
 * solta, um beco sem saida para quem acabou de abrir a tela).
 */
@Composable
fun EstadoVazio(
    titulo: String,
    texto: String,
    modifier: Modifier = Modifier,
    textoAcao: String? = null,
    aoAcionar: (() -> Unit)? = null
) {
    Column(
        modifier.fillMaxSize().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(texto, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        if (textoAcao != null && aoAcionar != null) {
            Spacer(Modifier.height(20.dp))
            BotaoPrimario(textoAcao, aoClicar = aoAcionar)
        }
    }
}
