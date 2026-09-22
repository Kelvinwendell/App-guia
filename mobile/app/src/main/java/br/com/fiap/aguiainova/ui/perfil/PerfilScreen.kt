package br.com.fiap.aguiainova.ui.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import br.com.fiap.aguiainova.domain.model.Perfil
import br.com.fiap.aguiainova.ui.common.containerDoApp
import br.com.fiap.aguiainova.ui.components.CartaoAguia
import br.com.fiap.aguiainova.ui.components.LinhaInfo
import br.com.fiap.aguiainova.ui.components.TelaBase
import br.com.fiap.aguiainova.ui.theme.AguiaCores

@Composable
fun PerfilScreen(perfil: Perfil, navController: NavController) {
    val auth = containerDoApp().auth
    val usuario by auth.usuario.collectAsStateWithLifecycle()

    TelaBase(titulo = "Perfil", navController = navController, perfil = perfil) { modificador ->
        Column(modificador, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier.size(96.dp).clip(CircleShape).background(AguiaCores.Primaria),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(52.dp))
            }
            Text(usuario?.nome.orEmpty(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(usuario?.email.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            CartaoAguia {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LinhaInfo("Perfil de acesso", perfil.rotulo.uppercase())
                    usuario?.cargo?.let { LinhaInfo("Cargo", it) }
                    usuario?.departamento?.let { LinhaInfo("Departamento", it) }
                    usuario?.unidade?.let { LinhaInfo("Unidade", it) }
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = auth::sair,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AguiaCores.Erro)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Text("  Sair da conta")
            }
        }
    }
}
