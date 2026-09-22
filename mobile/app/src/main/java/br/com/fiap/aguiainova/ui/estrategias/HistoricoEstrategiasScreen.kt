package br.com.fiap.aguiainova.ui.estrategias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.aguiainova.domain.model.AcaoEstrategia
import br.com.fiap.aguiainova.ui.common.viewModelDoApp
import br.com.fiap.aguiainova.ui.components.CampoTexto
import br.com.fiap.aguiainova.ui.components.CartaoAguia
import br.com.fiap.aguiainova.ui.components.ConteudoDoEstado
import br.com.fiap.aguiainova.ui.components.LinhaInfo
import br.com.fiap.aguiainova.ui.components.MensagemVazia
import br.com.fiap.aguiainova.ui.components.Selo
import br.com.fiap.aguiainova.ui.components.TelaBase
import br.com.fiap.aguiainova.ui.theme.AguiaCores
import br.com.fiap.aguiainova.util.Formatadores

@Composable
fun HistoricoEstrategiasScreen(aoVoltar: () -> Unit) {
    val vm = viewModelDoApp { HistoricoEstrategiasViewModel(it.estrategias) }
    val estado by vm.estado.collectAsStateWithLifecycle()
    val aviso by vm.aviso.collectAsStateWithLifecycle()
    var categoria by remember { mutableStateOf("") }
    var campanha by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { vm.carregar() }

    TelaBase(
        titulo = "Histórico de estratégias",
        subtitulo = "Tudo o que foi criado, alterado ou removido.",
        aoVoltar = aoVoltar,
        aviso = aviso,
        aoAvisoLido = vm::avisoLido
    ) { modificador ->
        Column(modificador, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CampoTexto(categoria, { categoria = it }, "Categoria", Modifier.weight(1f))
                CampoTexto(campanha, { campanha = it }, "Campanha", Modifier.weight(1f))
            }
            Button(onClick = { vm.carregar(categoria, campanha) }) { Text("Filtrar") }
            ConteudoDoEstado(estado, aoTentarNovamente = { vm.carregar(categoria, campanha) }, modifier = Modifier.weight(1f)) { registros ->
                if (registros.isEmpty()) {
                    MensagemVazia("Nenhum registro encontrado.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(registros, key = { it.id }) { registro ->
                            CartaoAguia {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Selo(registro.acao.rotulo, corDaAcao(registro.acao))
                                        Selo(registro.categoria, AguiaCores.Primaria)
                                    }
                                    Text(registro.titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    LinhaInfo("Data", Formatadores.dataHora(registro.data))
                                    LinhaInfo("Campanha", registro.campanha ?: "—")
                                    LinhaInfo("Por", registro.autorNome ?: "—")
                                    LinhaInfo("Id", registro.estrategiaId.takeLast(ULTIMOS_CARACTERES_DO_ID))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun corDaAcao(acao: AcaoEstrategia) = when (acao) {
    AcaoEstrategia.CRIADA -> AguiaCores.Sucesso
    AcaoEstrategia.ATUALIZADA -> AguiaCores.Alerta
    AcaoEstrategia.REMOVIDA -> AguiaCores.Erro
}

private const val ULTIMOS_CARACTERES_DO_ID = 8
