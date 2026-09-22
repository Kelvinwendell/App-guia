package br.com.fiap.aguiainova.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.aguiainova.domain.model.NovoUsuario
import br.com.fiap.aguiainova.domain.model.Perfil
import br.com.fiap.aguiainova.ui.common.viewModelDoApp
import br.com.fiap.aguiainova.ui.components.BotaoPrimario
import br.com.fiap.aguiainova.ui.components.CampoTexto
import br.com.fiap.aguiainova.ui.components.TelaBase

@Composable
fun RegistroScreen(aoConcluir: () -> Unit, aoVoltar: () -> Unit) {
    val vm = viewModelDoApp { AuthViewModel(it.auth, it.servidor) }
    val ocupado by vm.ocupado.collectAsStateWithLifecycle()
    val aviso by vm.aviso.collectAsStateWithLifecycle()
    var nome by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }
    var cargo by rememberSaveable { mutableStateOf("") }
    var departamento by rememberSaveable { mutableStateOf("") }
    var unidade by rememberSaveable { mutableStateOf("") }
    var perfil by rememberSaveable { mutableStateOf(Perfil.OPERADOR) }

    TelaBase(
        titulo = "Criar conta",
        aoVoltar = aoVoltar,
        aviso = aviso,
        aoAvisoLido = vm::avisoLido
    ) { modificador ->
        Column(
            modificador.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CampoTexto(nome, { nome = it }, "Nome")
            CampoTexto(email, { email = it }, "E-mail", teclado = KeyboardType.Email)
            CampoTexto(senha, { senha = it }, "Senha", senha = true, dica = "Mínimo de 6 caracteres")
            CampoTexto(cargo, { cargo = it }, "Cargo")
            CampoTexto(departamento, { departamento = it }, "Departamento")
            CampoTexto(unidade, { unidade = it }, "Unidade")
            Text("Perfil", style = MaterialTheme.typography.titleMedium)
            Perfil.entries.forEach { opcao ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = perfil == opcao, onClick = { perfil = opcao })
                    Text(opcao.rotulo)
                }
            }
            Spacer(Modifier.height(4.dp))
            BotaoPrimario(
                "Criar conta",
                aoClicar = {
                    vm.cadastrar(
                        NovoUsuario(nome, email, senha, perfil, cargo.ifBlank { null }, departamento.ifBlank { null }, unidade.ifBlank { null }),
                        aoConcluir
                    )
                },
                carregando = ocupado
            )
        }
    }
}
