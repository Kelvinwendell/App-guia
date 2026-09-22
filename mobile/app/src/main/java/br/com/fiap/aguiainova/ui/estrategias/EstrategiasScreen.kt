package br.com.fiap.aguiainova.ui.estrategias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import br.com.fiap.aguiainova.domain.model.Estrategia
import br.com.fiap.aguiainova.domain.model.Perfil
import br.com.fiap.aguiainova.navigation.Rotas
import br.com.fiap.aguiainova.ui.common.AoRetomar
import br.com.fiap.aguiainova.ui.common.viewModelDoApp
import br.com.fiap.aguiainova.ui.components.CartaoAguia
import br.com.fiap.aguiainova.ui.components.ConteudoDoEstado
import br.com.fiap.aguiainova.ui.components.DialogoConfirmacao
import br.com.fiap.aguiainova.ui.components.MensagemVazia
import br.com.fiap.aguiainova.ui.components.Selo
import br.com.fiap.aguiainova.ui.components.TelaBase
import br.com.fiap.aguiainova.ui.theme.AguiaCores
import br.com.fiap.aguiainova.ui.theme.cor

@Composable
fun EstrategiasScreen(perfil: Perfil, navController: NavController) {
    val vm = viewModelDoApp { EstrategiasViewModel(it.estrategias) }
    val estado by vm.estado.collectAsStateWithLifecycle()
    val aviso by vm.aviso.collectAsStateWithLifecycle()
    AoRetomar(vm::carregar)
    var paraExcluir by remember { mutableStateOf<Estrategia?>(null) }
    val lideranca = perfil == Perfil.LIDERANCA

    TelaBase(
        titulo = "Estratégias da Empresa",
        subtitulo = if (lideranca) "Gerencie as diretrizes do time." else "Consulte as diretrizes vigentes.",
        navController = navController,
        perfil = perfil,
        aviso = aviso,
        aoAvisoLido = vm::avisoLido,
        acoes = {
            IconButton(onClick = { navController.navigate(Rotas.HISTORICO_ESTRATEGIAS) }) {
                Icon(Icons.Default.History, contentDescription = "Histórico")
            }
        },
        botaoFlutuante = {
            if (lideranca) {
                FloatingActionButton(onClick = { navController.navigate(Rotas.estrategiaForm()) }, containerColor = AguiaCores.Primaria) {
                    Icon(Icons.Default.Add, contentDescription = "Nova estratégia")
                }
            }
        }
    ) { modificador ->
        ConteudoDoEstado(estado, aoTentarNovamente = vm::carregar, modifier = modificador) { lista ->
            if (lista.isEmpty()) {
                MensagemVazia("Nenhuma estratégia cadastrada.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
                    items(lista, key = { it.id }) { estrategia ->
                        CartaoEstrategia(
                            estrategia = estrategia,
                            editavel = lideranca,
                            aoEditar = { navController.navigate(Rotas.estrategiaForm(estrategia.id)) },
                            aoExcluir = { paraExcluir = estrategia }
                        )
                    }
                }
            }
        }
    }

    paraExcluir?.let { estrategia ->
        DialogoConfirmacao(
            titulo = "Excluir estratégia?",
            texto = "\"${estrategia.titulo}\" será removida. O histórico é mantido.",
            aoConfirmar = { vm.excluir(estrategia.id); paraExcluir = null },
            aoCancelar = { paraExcluir = null }
        )
    }
}

@Composable
private fun CartaoEstrategia(
    estrategia: Estrategia,
    editavel: Boolean,
    aoEditar: () -> Unit,
    aoExcluir: () -> Unit
) {
    CartaoAguia {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(estrategia.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(estrategia.descricao, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Selo(estrategia.status.rotulo, estrategia.status.cor())
                Selo(estrategia.categoria, AguiaCores.Primaria)
                estrategia.campanha?.let { Selo(it, AguiaCores.Alerta) }
            }
            if (editavel) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = aoEditar) { Text("Editar") }
                    TextButton(onClick = aoExcluir) { Text("Excluir", color = AguiaCores.Erro) }
                }
            }
        }
    }
}
