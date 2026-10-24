package br.com.fiap.aguiainova.ui.estrategias

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import br.com.fiap.aguiainova.domain.model.Estrategia
import br.com.fiap.aguiainova.domain.model.LeituraSaude
import br.com.fiap.aguiainova.domain.model.Perfil
import br.com.fiap.aguiainova.domain.model.SaudeEstrategia
import br.com.fiap.aguiainova.navigation.Rotas
import br.com.fiap.aguiainova.ui.common.AoRetomar
import br.com.fiap.aguiainova.ui.common.Estado
import br.com.fiap.aguiainova.ui.common.viewModelDoApp
import br.com.fiap.aguiainova.ui.components.BotaoPrimario
import br.com.fiap.aguiainova.ui.components.CartaoAguia
import br.com.fiap.aguiainova.ui.components.ConteudoDoEstado
import br.com.fiap.aguiainova.ui.components.DialogoConfirmacao
import br.com.fiap.aguiainova.ui.components.LinhaInfo
import br.com.fiap.aguiainova.ui.components.LinhaQuebravel
import br.com.fiap.aguiainova.ui.components.Selo
import br.com.fiap.aguiainova.ui.components.TelaBase
import br.com.fiap.aguiainova.ui.theme.AguiaCores
import br.com.fiap.aguiainova.ui.theme.cor
import br.com.fiap.aguiainova.util.Formatadores

/**
 * Uma estrategia por inteiro: o plano (metas e periodo) e, para GESTOR e LIDERANCA, a saude calculada por regras com
 * o porque e a proxima acao, mais a leitura da IA sob demanda. A lideranca edita e exclui daqui.
 */
@Composable
fun EstrategiaDetalheScreen(id: String, perfil: Perfil, navController: NavController) {
    val vm = viewModelDoApp(chave = "estrategia-detalhe-$id") { EstrategiaDetalheViewModel(id, perfil, it.estrategias, it.ia) }
    val estado by vm.estado.collectAsStateWithLifecycle()
    val leitura by vm.leitura.collectAsStateWithLifecycle()
    val ocupado by vm.ocupado.collectAsStateWithLifecycle()
    val aviso by vm.aviso.collectAsStateWithLifecycle()
    AoRetomar(vm::carregar)
    var excluindo by remember { mutableStateOf(false) }

    TelaBase(
        titulo = "Estratégia",
        aoVoltar = { navController.popBackStack() },
        aviso = aviso,
        aoAvisoLido = vm::avisoLido
    ) { modificador ->
        ConteudoDoEstado(estado, aoTentarNovamente = vm::carregar, modifier = modificador) { detalhe ->
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PlanoDaEstrategia(detalhe.estrategia)
                if (perfil != Perfil.OPERADOR && detalhe.estrategia.vigente) {
                    detalhe.saude?.let { SaudeDaEstrategia(it) }
                    LeituraDaIa(leitura, aoLer = vm::lerComIa)
                }
                if (perfil == Perfil.OPERADOR && detalhe.estrategia.vigente) {
                    Text(
                        "Tem uma ideia que ajuda nisso? Cadastre em Ideias e vincule a esta estratégia: " +
                            "ideias alinhadas são avaliadas com mais facilidade.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (perfil == Perfil.LIDERANCA) {
                    BotaoPrimario("Editar estratégia", aoClicar = { navController.navigate(Rotas.estrategiaForm(id)) })
                    TextButton(onClick = { excluindo = true }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text("Excluir estratégia", color = AguiaCores.Erro)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (excluindo) {
        val titulo = (estado as? Estado.Pronto)?.dados?.estrategia?.titulo
        DialogoConfirmacao(
            titulo = "Excluir estratégia?",
            texto = (titulo?.let { "\"$it\" será removida." } ?: "A estratégia será removida.") +
                " O histórico é mantido. " +
                "Se houver ideias ou projetos vinculados, encerre-a em vez de excluir.",
            aoConfirmar = {
                excluindo = false
                if (!ocupado) vm.excluir { navController.popBackStack() }
            },
            aoCancelar = { excluindo = false }
        )
    }
}

@Composable
private fun PlanoDaEstrategia(estrategia: Estrategia) {
    CartaoAguia {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(estrategia.titulo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            LinhaQuebravel {
                Selo(estrategia.status.rotulo, estrategia.status.cor())
                Selo(estrategia.categoria, AguiaCores.Primaria)
                estrategia.campanha?.let { Selo(it, AguiaCores.Alerta) }
            }
            Text(estrategia.descricao, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LinhaInfo("Período", Formatadores.periodo(estrategia.inicio, estrategia.fim) ?: "Não definido")
            Text("Metas", fontWeight = FontWeight.Bold)
            if (estrategia.metas.isEmpty()) {
                Text("Nenhuma meta cadastrada.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            estrategia.metas.forEach { Text("• $it") }
            estrategia.autorNome?.let { LinhaInfo("Definida por", it) }
        }
    }
}

@Composable
private fun SaudeDaEstrategia(saude: SaudeEstrategia) {
    CartaoAguia {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Saúde da estratégia",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Selo(saude.nivel.rotulo, saude.nivel.cor())
            }
            saude.motivos.forEach { Text("• $it") }
            Text("Próxima ação: ${saude.proximaAcao}", fontWeight = FontWeight.SemiBold, color = saude.nivel.cor())
            Text("Números", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            LinhaInfo("Ideias", "${saude.ideias} · ${saude.ideiasEmAnalise} em análise")
            LinhaInfo("Aprovadas sem projeto", saude.aprovadasSemProjeto.toString())
            LinhaInfo("Projetos", "${saude.projetos} · ${saude.projetosAtrasados} em atraso")
            if (saude.projetos > 0) {
                LinhaInfo("Investimento", Formatadores.moeda(saude.investimento))
                LinhaInfo("Retorno", Formatadores.moeda(saude.retorno))
                LinhaInfo("ROI", Formatadores.percentual(saude.roiPercentual))
            }
            LinhaInfo("Última atividade", saude.ultimaAtividade?.let { Formatadores.data(it) } ?: "Nenhuma")
        }
    }
}

/** A IA explica o diagnostico e recomenda; so e chamada quando a pessoa pede (cada chamada gasta a cota da IA). */
@Composable
private fun LeituraDaIa(estado: Estado<LeituraSaude>?, aoLer: () -> Unit) {
    CartaoAguia {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            when (estado) {
                null -> Text(
                    "Peça à IA uma leitura de por que a estratégia está assim e do que fazer primeiro.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Estado.Carregando -> CircularProgressIndicator()
                is Estado.Erro -> Text(estado.mensagem, color = AguiaCores.Erro)
                is Estado.Pronto -> ConteudoDaLeitura(estado.dados)
            }
            Button(onClick = aoLer, enabled = estado != Estado.Carregando) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Text(if (estado == null) "  Ler com IA" else "  Ler novamente")
            }
        }
    }
}

@Composable
private fun ConteudoDaLeitura(leitura: LeituraSaude) {
    Selo(
        if (leitura.geradaPorIa) "Gerado por IA · ${leitura.fonte.lowercase()}" else "Leitura por regras (IA indisponível)",
        AguiaCores.Primaria
    )
    Text(leitura.leitura, fontWeight = FontWeight.SemiBold)
    if (leitura.recomendacoes.isNotEmpty()) {
        Text("Recomendações", fontWeight = FontWeight.Bold, color = AguiaCores.Sucesso)
        leitura.recomendacoes.forEach { Text("→ $it") }
    }
}
