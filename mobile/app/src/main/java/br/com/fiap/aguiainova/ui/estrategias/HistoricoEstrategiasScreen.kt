package br.com.fiap.aguiainova.ui.estrategias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.aguiainova.domain.model.AcaoEstrategia
import br.com.fiap.aguiainova.ui.components.CartaoAguia
import br.com.fiap.aguiainova.ui.components.ConteudoDoEstado
import br.com.fiap.aguiainova.ui.components.EstadoVazio
import br.com.fiap.aguiainova.ui.components.LinhaInfo
import br.com.fiap.aguiainova.ui.components.Selo
import br.com.fiap.aguiainova.ui.components.Seletor
import br.com.fiap.aguiainova.ui.theme.AguiaCores
import br.com.fiap.aguiainova.util.Formatadores

/**
 * Aba "Histórico" das estrategias: tudo o que foi criado, alterado ou removido. Os filtros sao listas das categorias e
 * campanhas que existem no proprio historico (antes eram campos de texto que so achavam o nome exato).
 */
@Composable
fun ConteudoHistorico(vm: HistoricoEstrategiasViewModel, modifier: Modifier = Modifier) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    var categoria by rememberSaveable { mutableStateOf<String?>(null) }
    var campanha by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { vm.carregar() }

    ConteudoDoEstado(estado, aoTentarNovamente = vm::carregar, modifier = modifier) { registros ->
        val categorias = registros.map { it.categoria }.distinct().sorted()
        val campanhas = registros.mapNotNull { it.campanha }.distinct().sorted()
        val exibidos = registros.filter { registro ->
            (categoria == null || registro.categoria.equals(categoria, ignoreCase = true)) &&
                (campanha == null || registro.campanha.equals(campanha, ignoreCase = true))
        }
        if (registros.isEmpty()) {
            EstadoVazio(
                "Nada no histórico ainda",
                "Cada estratégia criada, alterada ou removida pela liderança fica registrada aqui, com data e autor."
            )
        } else Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (categorias.size > 1 || campanhas.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                    Seletor("Categoria", categorias, categoria, { it }, { categoria = it }, Modifier.weight(1f), "Todas")
                    Seletor("Campanha", campanhas, campanha, { it }, { campanha = it }, Modifier.weight(1f), "Todas")
                }
            }
            if (exibidos.isEmpty()) {
                EstadoVazio("Nenhum registro com esse filtro", "Escolha outra categoria ou campanha.")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp)
                ) {
                    items(exibidos, key = { it.id }) { registro ->
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
