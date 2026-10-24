package br.com.fiap.aguiainova.ui.estrategias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.aguiainova.domain.model.AcaoEstrategia
import br.com.fiap.aguiainova.ui.components.CartaoAguia
import br.com.fiap.aguiainova.ui.components.ConteudoDoEstado
import br.com.fiap.aguiainova.ui.components.EstadoVazio
import br.com.fiap.aguiainova.ui.components.LadoALadoOuEmpilhado
import br.com.fiap.aguiainova.ui.components.LinhaInfo
import br.com.fiap.aguiainova.ui.components.LinhaQuebravel
import br.com.fiap.aguiainova.ui.components.Selo
import br.com.fiap.aguiainova.ui.components.Seletor
import br.com.fiap.aguiainova.ui.theme.AguiaCores
import br.com.fiap.aguiainova.util.Formatadores

/**
 * Aba "Histórico" das estrategias: tudo o que foi criado, alterado ou removido. Os filtros sao listas das categorias e
 * campanhas que existem no historico; o valor escolhido vai para a API (via ViewModel), nao e filtrado na tela.
 */
@Composable
fun ConteudoHistorico(vm: HistoricoEstrategiasViewModel, modifier: Modifier = Modifier) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val filtro by vm.filtro.collectAsStateWithLifecycle()
    val opcoes by vm.opcoes.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.carregar() }

    ConteudoDoEstado(estado, aoTentarNovamente = vm::carregar, modifier = modifier) { registros ->
        if (registros.isEmpty() && filtro.vazio) {
            EstadoVazio(
                "Nada no histórico ainda",
                "Cada estratégia criada, alterada ou removida pela liderança fica registrada aqui, com data e autor."
            )
        } else Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (opcoes.categorias.size > 1 || opcoes.campanhas.isNotEmpty()) {
                LadoALadoOuEmpilhado(Modifier.padding(top = 4.dp)) { largura ->
                    Seletor(
                        "Categoria", opcoes.categorias, filtro.categoria, { it },
                        { vm.filtrarPor(it, filtro.campanha) }, largura, "Todas"
                    )
                    Seletor(
                        "Campanha", opcoes.campanhas, filtro.campanha, { it },
                        { vm.filtrarPor(filtro.categoria, it) }, largura, "Todas"
                    )
                }
            }
            if (registros.isEmpty()) {
                EstadoVazio("Nenhum registro com esse filtro", "Escolha outra categoria ou campanha.")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp)
                ) {
                    items(registros, key = { it.id }) { registro ->
                        CartaoAguia {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                LinhaQuebravel {
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
