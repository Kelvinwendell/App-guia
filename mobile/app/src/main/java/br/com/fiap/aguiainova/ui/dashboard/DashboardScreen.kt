package br.com.fiap.aguiainova.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import br.com.fiap.aguiainova.domain.model.InsightsIa
import br.com.fiap.aguiainova.domain.model.Perfil
import br.com.fiap.aguiainova.domain.model.ResumoDashboard
import br.com.fiap.aguiainova.domain.model.StatusProjeto
import br.com.fiap.aguiainova.ui.common.AoRetomar
import br.com.fiap.aguiainova.ui.common.Estado
import br.com.fiap.aguiainova.ui.common.viewModelDoApp
import br.com.fiap.aguiainova.ui.components.CartaoAguia
import br.com.fiap.aguiainova.ui.components.CartaoIndicador
import br.com.fiap.aguiainova.ui.components.ConteudoDoEstado
import br.com.fiap.aguiainova.ui.components.GraficoBarras
import br.com.fiap.aguiainova.ui.components.GraficoColunas
import br.com.fiap.aguiainova.ui.components.ItemGrafico
import br.com.fiap.aguiainova.ui.components.LinhaInfo
import br.com.fiap.aguiainova.ui.components.Selo
import br.com.fiap.aguiainova.ui.components.Seletor
import br.com.fiap.aguiainova.ui.components.TelaBase
import br.com.fiap.aguiainova.ui.components.TituloSecao
import br.com.fiap.aguiainova.ui.theme.AguiaCores
import br.com.fiap.aguiainova.ui.theme.cor
import br.com.fiap.aguiainova.util.Formatadores

@Composable
fun DashboardScreen(navController: NavController) {
    val vm = viewModelDoApp { DashboardViewModel(it.dashboard, it.ia) }
    val estado by vm.estado.collectAsStateWithLifecycle()
    val filtro by vm.estrategiaFiltro.collectAsStateWithLifecycle()
    val insights by vm.insights.collectAsStateWithLifecycle()
    val aviso by vm.aviso.collectAsStateWithLifecycle()
    AoRetomar(vm::carregar)

    TelaBase(
        titulo = "Painel Estratégico",
        subtitulo = "Acompanhe os indicadores da inovação.",
        navController = navController,
        perfil = Perfil.LIDERANCA,
        aviso = aviso,
        aoAvisoLido = vm::avisoLido
    ) { modificador ->
        ConteudoDoEstado(estado, aoTentarNovamente = vm::carregar, modifier = modificador) { dados ->
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val estrategias = dados.porEstrategia.filter { it.estrategiaId != null }
                Seletor(
                    rotulo = "Estratégia",
                    opcoes = estrategias,
                    selecionado = estrategias.firstOrNull { it.estrategiaId == filtro },
                    rotuloDe = { it.titulo },
                    aoSelecionar = { vm.filtrarPorEstrategia(it?.estrategiaId) },
                    textoVazio = "Todas as estratégias"
                )
                Indicadores(dados.resumo)
                TituloSecao("ROI por estratégia")
                CartaoAguia {
                    Column(Modifier.padding(16.dp)) {
                        val itens = dados.porEstrategia.filter { it.roiPercentual != null }
                            .map { ItemGrafico(it.titulo, it.roiPercentual ?: 0.0) }
                        if (itens.isEmpty()) Text("Sem dados de ROI ainda.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        else GraficoBarras(itens, Formatadores::percentual)
                    }
                }
                TituloSecao("Projetos por status")
                CartaoAguia {
                    Column(Modifier.padding(16.dp)) {
                        GraficoBarras(
                            itens = StatusProjeto.entries.map { ItemGrafico(it.rotulo, (dados.resumo.projetosPorStatus[it.name] ?: 0L).toDouble(), it.cor()) },
                            formatarValor = { it.toInt().toString() }
                        )
                    }
                }
                TituloSecao("Retorno por mês")
                CartaoAguia {
                    Column(Modifier.padding(16.dp)) {
                        if (dados.evolucao.isEmpty()) Text("Sem resultados registrados ainda.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        else {
                            GraficoColunas(dados.evolucao.map { ItemGrafico(Formatadores.mes(it.mes), it.retornoNoMes) })
                            Spacer(Modifier.height(8.dp))
                            LinhaInfo("Acumulado", Formatadores.moeda(dados.evolucao.last().retornoAcumulado))
                        }
                    }
                }
                TituloSecao("Ideias")
                CartaoAguia {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        val ideias = dados.resumo.ideias
                        LinhaInfo("Total", ideias.total.toString())
                        LinhaInfo("Em análise", ideias.emAnalise.toString())
                        LinhaInfo("Aprovadas", ideias.aprovadas.toString())
                        LinhaInfo("Rejeitadas", ideias.rejeitadas.toString())
                        LinhaInfo("Taxa de aprovação", Formatadores.percentual(ideias.taxaAprovacaoPercentual))
                    }
                }
                if (dados.resumo.proximosPrazos.isNotEmpty()) {
                    TituloSecao("Próximos prazos")
                    CartaoAguia {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            dados.resumo.proximosPrazos.forEach { prazo ->
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.padding(0.dp)) {
                                    Column(Modifier.weight(1f)) {
                                        Text(prazo.nome, fontWeight = FontWeight.SemiBold)
                                        Text("${Formatadores.data(prazo.prazo)} · ${prazo.progresso}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Selo(if (prazo.atrasado) "Atrasado" else prazo.status.rotulo, if (prazo.atrasado) AguiaCores.Erro else prazo.status.cor())
                                }
                            }
                        }
                    }
                }
                TituloSecao("Insights de IA")
                BlocoInsights(insights, aoGerar = vm::gerarInsights)
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun Indicadores(resumo: ResumoDashboard) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CartaoIndicador("ROI", Formatadores.percentual(resumo.roiPercentual), AguiaCores.Sucesso, Modifier.weight(1f))
        CartaoIndicador("Lucro", Formatadores.moedaCompacta(resumo.lucroTotal), if (resumo.lucroTotal < 0) AguiaCores.Erro else AguiaCores.Sucesso, Modifier.weight(1f))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CartaoIndicador("Investimento", Formatadores.moedaCompacta(resumo.investimentoTotal), AguiaCores.Alerta, Modifier.weight(1f))
        CartaoIndicador("Retorno", Formatadores.moedaCompacta(resumo.retornoTotal), AguiaCores.Primaria, Modifier.weight(1f))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CartaoIndicador("Produtividade", Formatadores.percentualComSinal(resumo.ganhoProdutividadeMedio), AguiaCores.Primaria, Modifier.weight(1f))
        CartaoIndicador("Redução de custos", Formatadores.percentual(resumo.reducaoCustosMedia), AguiaCores.Alerta, Modifier.weight(1f))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CartaoIndicador("Projetos", resumo.totalProjetos.toString(), MaterialTheme.colorScheme.onSurfaceVariant, Modifier.weight(1f))
        CartaoIndicador("Atrasados", resumo.projetosAtrasados.toString(), if (resumo.projetosAtrasados > 0) AguiaCores.Erro else AguiaCores.Sucesso, Modifier.weight(1f))
    }
}

@Composable
private fun BlocoInsights(estado: Estado<InsightsIa>?, aoGerar: () -> Unit) {
    CartaoAguia {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            when (estado) {
                null -> Text("Peça à IA uma leitura executiva dos resultados e sugestões de melhoria.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Estado.Carregando -> CircularProgressIndicator()
                is Estado.Erro -> Text(estado.mensagem, color = AguiaCores.Erro)
                is Estado.Pronto -> ConteudoDeInsights(estado.dados)
            }
            Button(onClick = aoGerar, enabled = estado != Estado.Carregando) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Text(if (estado == null) "  Gerar insights" else "  Gerar novamente")
            }
        }
    }
}

@Composable
private fun ConteudoDeInsights(insights: InsightsIa) {
    val geradaPorIa = insights.fonte != "HEURISTICA"
    Selo(if (geradaPorIa) "Gerado por IA · ${insights.fonte.lowercase()}" else "Leitura por regras (IA indisponível)", AguiaCores.Primaria)
    Text(insights.resumo, fontWeight = FontWeight.SemiBold)
    insights.insights.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
    if (insights.recomendacoes.isNotEmpty()) {
        Text("Recomendações", fontWeight = FontWeight.Bold, color = AguiaCores.Sucesso)
        insights.recomendacoes.forEach { Text("→ $it", style = MaterialTheme.typography.bodyMedium) }
    }
}
