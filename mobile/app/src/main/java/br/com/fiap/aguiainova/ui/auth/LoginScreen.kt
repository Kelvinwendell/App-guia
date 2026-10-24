package br.com.fiap.aguiainova.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.aguiainova.domain.model.Perfil
import br.com.fiap.aguiainova.ui.common.viewModelDoApp
import br.com.fiap.aguiainova.ui.components.BotaoPrimario
import br.com.fiap.aguiainova.ui.components.CampoTexto
import br.com.fiap.aguiainova.ui.components.TelaBase

@Composable
fun LoginScreen(aoEntrar: (Perfil) -> Unit, aoCriarConta: () -> Unit) {
    val vm = viewModelDoApp { AuthViewModel(it.auth, it.servidor) }
    val ocupado by vm.ocupado.collectAsStateWithLifecycle()
    val aviso by vm.aviso.collectAsStateWithLifecycle()
    val url by vm.urlServidor.collectAsStateWithLifecycle()
    var email by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }
    var editandoServidor by remember { mutableStateOf(false) }

    TelaBase(
        titulo = "Águia Inova",
        subtitulo = "Gestão da inovação empresarial",
        aviso = aviso,
        aoAvisoLido = vm::avisoLido
    ) { modificador ->
        Column(
            modificador.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            CampoTexto(email, { email = it }, "E-mail", teclado = KeyboardType.Email)
            CampoTexto(senha, { senha = it }, "Senha", senha = true)
            Spacer(Modifier.height(4.dp))
            BotaoPrimario("Entrar", aoClicar = { vm.entrar(email, senha, aoEntrar) }, carregando = ocupado)
            TextButton(onClick = aoCriarConta, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Criar conta")
            }
            if (vm.servidorEditavel) {
                TextButton(
                    onClick = { editandoServidor = true },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Servidor: $url")
                }
            }
        }
    }

    if (vm.servidorEditavel && editandoServidor) {
        DialogoServidor(
            urlAtual = url,
            aoSalvar = { if (vm.salvarServidor(it)) editandoServidor = false },
            aoRestaurar = {
                vm.restaurarServidor()
                editandoServidor = false
            },
            aoCancelar = { editandoServidor = false }
        )
    }
}

@Composable
private fun DialogoServidor(
    urlAtual: String,
    aoSalvar: (String) -> Unit,
    aoRestaurar: () -> Unit,
    aoCancelar: () -> Unit
) {
    var texto by remember { mutableStateOf(urlAtual) }
    AlertDialog(
        onDismissRequest = aoCancelar,
        title = { Text("Servidor da API") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CampoTexto(
                    texto, { texto = it }, "Endereço",
                    teclado = KeyboardType.Uri,
                    dica = "Emulador: http://10.0.2.2:8080 · Aparelho: IP do computador, ex.: 192.168.0.10:8080"
                )
            }
        },
        confirmButton = { TextButton(onClick = { aoSalvar(texto) }) { Text("Salvar") } },
        dismissButton = {
            Column {
                TextButton(onClick = aoRestaurar) { Text("Restaurar padrão") }
                TextButton(onClick = aoCancelar) { Text("Cancelar") }
            }
        }
    )
}
