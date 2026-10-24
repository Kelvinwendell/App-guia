package br.com.fiap.aguiainova.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun CampoTexto(
    valor: String,
    aoMudar: (String) -> Unit,
    rotulo: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    senha: Boolean = false,
    linhas: Int = 1,
    teclado: KeyboardType = KeyboardType.Text,
    dica: String? = null,
    habilitado: Boolean = true
) {
    OutlinedTextField(
        value = valor,
        onValueChange = aoMudar,
        label = { Text(rotulo) },
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        singleLine = linhas == 1,
        minLines = linhas,
        enabled = habilitado,
        visualTransformation = if (senha) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (senha) KeyboardType.Password else teclado),
        supportingText = dica?.let { texto -> { Text(texto) } }
    )
}

/**
 * Lista suspensa. [textoVazio], quando informado, acrescenta a opcao "nenhum" (que chama [aoSelecionar] com nulo).
 */
@Composable
fun <T> Seletor(
    rotulo: String,
    opcoes: List<T>,
    selecionado: T?,
    rotuloDe: (T) -> String,
    aoSelecionar: (T?) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    textoVazio: String? = null
) {
    var aberto by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedButton(
            onClick = { aberto = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text(rotulo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(selecionado?.let(rotuloDe) ?: textoVazio ?: "Selecione")
            }
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
            if (textoVazio != null) {
                DropdownMenuItem(text = { Text(textoVazio) }, onClick = { aoSelecionar(null); aberto = false })
            }
            opcoes.forEach { opcao ->
                DropdownMenuItem(text = { Text(rotuloDe(opcao)) }, onClick = { aoSelecionar(opcao); aberto = false })
            }
        }
    }
}

@Composable
fun BotaoPrimario(
    texto: String,
    aoClicar: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    carregando: Boolean = false,
    habilitado: Boolean = true
) {
    Button(
        onClick = aoClicar,
        enabled = habilitado && !carregando,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(50)
    ) {
        if (carregando) {
            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
        } else {
            Text(texto)
        }
    }
}
