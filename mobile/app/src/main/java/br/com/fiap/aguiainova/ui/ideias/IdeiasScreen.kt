package br.com.fiap.aguiainova.ui.ideias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.navigation.NavController
import br.com.fiap.aguiainova.domain.model.AvaliacaoIa
import br.com.fiap.aguiainova.domain.model.AvaliacaoIdeia
import br.com.fiap.aguiainova.domain.model.Ideia
import br.com.fiap.aguiainova.domain.model.Perfil
import br.com.fiap.aguiainova.domain.model.Prioridade
import br.com.fiap.aguiainova.domain.model.StatusIdeia
import br.com.fiap.aguiainova.navigation.Rotas
import br.com.fiap.aguiainova.ui.common.AoRetomar
import br.com.fiap.aguiainova.ui.common.viewModelDoApp
import br.com.fiap.aguiainova.ui.components.AbasDaTela
import br.com.fiap.aguiainova.ui.components.CampoTexto
import br.com.fiap.aguiainova.ui.components.CartaoAguia
import br.com.fiap.aguiainova.ui.components.ConteudoDoEstado
import br.com.fiap.aguiainova.ui.components.DialogoConfirmacao
import br.com.fiap.aguiainova.ui.components.MensagemVazia
import br.com.fiap.aguiainova.ui.components.MostrarNovoNoTopo
import br.com.fiap.aguiainova.ui.components.LinhaQuebravel
import br.com.fiap.aguiainova.ui.components.Selo
import br.com.fiap.aguiainova.ui.components.Seletor
import br.com.fiap.aguiainova.ui.components.TelaBase
import br.com.fiap.aguiainova.ui.theme.AguiaCores
import br.com.fiap.aguiainova.ui.theme.cor

@Composable
fun IdeiasScreen(perfil: Perfil, navController: NavController) {
    val vm = viewModelDoApp(chave = "ideias-${perfil.name}") { IdeiasViewModel(perfil, it.ideias, it.ia) }
    val estado by vm.estado.collectAsStateWithLifecycle()
    val filtro by vm.filtro.collectAsStateWithLifecycle()
    val aprovadas by vm.somenteAprovadas.collectAsStateWithLifecycle()
    val ordenarPorIa by vm.ordenarPorIa.collectAsStateWithLifecycle()
    val ocupado by vm.ocupado.collectAsStateWithLifecycle()
    val aviso by vm.aviso.collectAsStateWithLifecycle()
    AoRetomar(vm::carregar)

    var ideiaParaExcluir by remember { mutableStateOf<Ideia?>(null) }
    var decisao by remember { mutableStateOf<Pair<Ideia, StatusIdeia>?>(null) }
    val gestor = perfil == Perfil.GESTOR

    TelaBase(
        titulo = "Ideias",
        subtitulo = if (gestor) "Analise, priorize e aprove as ideias." else "Acompanhe as suas e veja o que a empresa já aprovou.",
        navController = navController,
        perfil = perfil,
        aviso = aviso,
        aoAvisoLido = vm::avisoLido,
        acoes = {
            if (gestor) {
                IconButton(onClick = vm::priorizarComIa, enabled = !ocupado) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "Priorizar com IA", tint = AguiaCores.Primaria)
                }
            }
        },
        botaoFlutuante = {
            if (!gestor) {
                FloatingActionButton(onClick = { navController.navigate(Rotas.ideiaForm()) }, containerColor = AguiaCores.Primaria) {
                    Icon(Icons.Default.Add, contentDescription = "Nova ideia")
                }
            }
        }
    ) { modificador ->
        Column(modificador) {
            if (gestor) {
                FiltrosDoGestor(filtro, ordenarPorIa, vm::filtrarPor, vm::ordenarPorNotaDaIa)
            } else {
                AbasDaTela(ABAS_DO_OPERADOR, if (aprovadas) 1 else 0, { vm.mostrarAprovadasDaEmpresa(it == 1) })
            }
            ConteudoDoEstado(estado, aoTentarNovamente = vm::carregar, modifier = Modifier.weight(1f)) { lista ->
                val exibidas = if (ordenarPorIa) lista.sortedByDescending { it.avaliacaoIa?.score ?: -1 } else lista
                if (exibidas.isEmpty()) {
                    val filtrado = gestor && filtro != null
                    when {
                        filtrado -> MensagemVazia("Nenhuma ideia encontrada com esse filtro.")
                        gestor -> MensagemVazia("Nenhuma ideia enviada ainda.")
                        aprovadas -> MensagemVazia("Nenhuma ideia aprovada pela empresa ainda.")
                        else -> MensagemVazia(
                            "Você ainda não cadastrou nenhuma ideia.",
                            acaoTexto = "Cadastrar ideia",
                            aoAcao = { navController.navigate(Rotas.ideiaForm()) }
                        )
                    }
                } else {
                    // A lista fica "ancorada" no item que estava visivel; ao reordenar pela nota da IA voltamos ao topo
                    // para o gestor ver os primeiros colocados, e nao o meio do ranking.
                    val posicao = rememberLazyListState()
                    val ordemPelaIa = if (ordenarPorIa) exibidas.map { it.id } else null
                    LaunchedEffect(ordemPelaIa) { posicao.scrollToItem(0) }
                    MostrarNovoNoTopo(posicao, exibidas.first().id)
                    LazyColumn(
                        state = posicao,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp)
                    ) {
                        items(exibidas, key = { it.id }) { ideia ->
                            CartaoIdeia(
                                ideia = ideia,
                                perfil = perfil,
                                mostrarAcoesDoAutor = !gestor && !aprovadas,
                                ocupado = ocupado,
                                aoEditar = { navController.navigate(Rotas.ideiaForm(ideia.id)) },
                                aoExcluir = { ideiaParaExcluir = ideia },
                                aoDecidir = { status -> decisao = ideia to status },
                                aoPontuar = { vm.pontuarComIa(ideia.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    ideiaParaExcluir?.let { ideia ->
        DialogoConfirmacao(
            titulo = "Excluir ideia?",
            texto = "\"${ideia.titulo}\" será removida.",
            aoConfirmar = { vm.excluir(ideia.id); ideiaParaExcluir = null },
            aoCancelar = { ideiaParaExcluir = null }
        )
    }
    decisao?.let { (ideia, status) ->
        DialogoDecisao(
            ideia = ideia,
            status = status,
            aoConfirmar = { avaliacao -> vm.decidir(ideia.id, avaliacao); decisao = null },
            aoCancelar = { decisao = null }
        )
    }
}

/** O operador alterna entre o que ele enviou e o que a empresa ja aprovou: duas partes da tela, entao sao abas. */
private val ABAS_DO_OPERADOR = listOf("Minhas ideias", "Aprovadas da empresa")

@Composable
private fun FiltrosDoGestor(
    filtro: StatusIdeia?,
    ordenarPorIa: Boolean,
    aoFiltrar: (StatusIdeia?) -> Unit,
    aoOrdenar: (Boolean) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
        item { FilterChip(selected = filtro == null, onClick = { aoFiltrar(null) }, label = { Text("Todas") }) }
        items(StatusIdeia.entries) { status ->
            FilterChip(selected = filtro == status, onClick = { aoFiltrar(status) }, label = { Text(status.rotulo) })
        }
        item {
            FilterChip(selected = ordenarPorIa, onClick = { aoOrdenar(!ordenarPorIa) }, label = { Text("Ordenar por nota IA") })
        }
    }
}

/** Nao e private: SPEC-RD-005 testa esse cartao isoladamente (CartaoIdeiaTest). */
@Composable
fun CartaoIdeia(
    ideia: Ideia,
    perfil: Perfil,
    mostrarAcoesDoAutor: Boolean,
    ocupado: Boolean,
    aoEditar: () -> Unit,
    aoExcluir: () -> Unit,
    aoDecidir: (StatusIdeia) -> Unit,
    aoPontuar: () -> Unit
) {
    CartaoAguia {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(ideia.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(ideia.descricao, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LinhaQuebravel {
                Selo(ideia.status.rotulo, ideia.status.cor())
                Selo("Prioridade ${ideia.prioridade.rotulo.lowercase()}", ideia.prioridade.cor())
            }
            Text(
                buildString {
                    append(ideia.categoria)
                    ideia.estrategiaTitulo?.let { append(" · Estratégia: $it") }
                    if (perfil == Perfil.GESTOR || ideia.autorNome.isNotBlank()) append(" · por ${ideia.autorNome}")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ideia.parecer?.let { Text("Parecer: $it", style = MaterialTheme.typography.bodySmall) }
            ideia.avaliacaoIa?.let { AvaliacaoDaIa(it) }

            if (perfil == Perfil.GESTOR) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { aoDecidir(StatusIdeia.APROVADA) },
                        enabled = !ocupado,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AguiaCores.Sucesso, contentColor = AguiaCores.Fundo)
                    ) { Text("Aprovar") }
                    Button(
                        onClick = { aoDecidir(StatusIdeia.REJEITADA) },
                        enabled = !ocupado,
                        modifier = Modifier.weight(1f),
                        // par error/onError explicito: o rosa claro de RD-001 nao garante contraste com texto
                        // escolhido "no olho" - usa o par que o tema ja define para isso.
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) { Text("Rejeitar") }
                }
                OutlinedButton(onClick = aoPontuar, enabled = !ocupado, modifier = Modifier.fillMaxWidth()) {
                    Text("Avaliar com IA")
                }
            } else if (mostrarAcoesDoAutor && ideia.editavelPeloAutor) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = aoEditar) { Text("Editar") }
                    TextButton(onClick = aoExcluir) { Text("Excluir", color = AguiaCores.Erro) }
                }
            }
        }
    }
}

/** Nota sugerida pela IA (ou pelas regras): e so uma orientacao para o gestor decidir. Superficie secundaria
 * (surfaceContainerHigh, mais clara que o cartao que a contem) para nao competir com o cartao da ideia. */
@Composable
private fun AvaliacaoDaIa(avaliacao: AvaliacaoIa) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Tres selos ("Nota", "Sugere: ...", origem) nao cabem numa linha em 360dp/fonte 150%: quebram em vez de vazar.
            LinhaQuebravel {
                Selo("Nota ${avaliacao.score}/100", AguiaCores.Primaria)
                Selo("Sugere: ${avaliacao.prioridadeSugerida.rotulo}", avaliacao.prioridadeSugerida.cor())
                Selo(if (avaliacao.geradaPorIa) "IA · ${avaliacao.fonte.lowercase()}" else "Regras", MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                "Alinhamento com a estratégia: ${avaliacao.alinhamentoEstrategico}/100",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(avaliacao.justificativa, style = MaterialTheme.typography.bodySmall)
            if (avaliacao.pontosFortes.isNotEmpty()) {
                Text("Pontos fortes: " + avaliacao.pontosFortes.joinToString("; "), style = MaterialTheme.typography.bodySmall, color = AguiaCores.Sucesso)
            }
            if (avaliacao.riscos.isNotEmpty()) {
                Text("Riscos: " + avaliacao.riscos.joinToString("; "), style = MaterialTheme.typography.bodySmall, color = AguiaCores.Alerta)
            }
        }
    }
}

@Composable
private fun DialogoDecisao(
    ideia: Ideia,
    status: StatusIdeia,
    aoConfirmar: (AvaliacaoIdeia) -> Unit,
    aoCancelar: () -> Unit
) {
    var prioridade by remember { mutableStateOf(ideia.prioridade) }
    var parecer by remember { mutableStateOf(ideia.parecer.orEmpty()) }
    AlertDialog(
        onDismissRequest = aoCancelar,
        title = { Text(if (status == StatusIdeia.APROVADA) "Aprovar ideia" else "Rejeitar ideia") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Seletor("Prioridade", Prioridade.entries, prioridade, { it.rotulo }, { prioridade = it ?: prioridade })
                CampoTexto(parecer, { parecer = it }, "Parecer (opcional)", linhas = 3)
            }
        },
        confirmButton = {
            TextButton(onClick = { aoConfirmar(AvaliacaoIdeia(status, prioridade, parecer.ifBlank { null })) }) { Text("Confirmar") }
        },
        dismissButton = { TextButton(onClick = aoCancelar) { Text("Cancelar") } }
    )
}
