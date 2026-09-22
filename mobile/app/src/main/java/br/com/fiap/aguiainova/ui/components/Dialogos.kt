package br.com.fiap.aguiainova.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import br.com.fiap.aguiainova.ui.theme.AguiaCores

@Composable
fun DialogoConfirmacao(
    titulo: String,
    texto: String,
    aoConfirmar: () -> Unit,
    aoCancelar: () -> Unit,
    textoConfirmar: String = "Excluir"
) {
    AlertDialog(
        onDismissRequest = aoCancelar,
        title = { Text(titulo) },
        text = { Text(texto) },
        confirmButton = {
            TextButton(onClick = aoConfirmar) { Text(textoConfirmar, color = AguiaCores.Erro) }
        },
        dismissButton = { TextButton(onClick = aoCancelar) { Text("Cancelar") } }
    )
}
