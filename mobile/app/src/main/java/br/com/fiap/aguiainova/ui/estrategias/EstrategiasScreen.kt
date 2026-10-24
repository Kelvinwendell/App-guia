package br.com.fiap.aguiainova.ui.estrategias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import br.com.fiap.aguiainova.domain.model.Estrategia
import br.com.fiap.aguiainova.domain.model.NivelSaude
import br.com.fiap.aguiainova.domain.model.Perfil
import br.com.fiap.aguiainova.domain.model.SaudeEstrategia
import br.com.fiap.aguiainova.navigation.Rotas
import br.com.fiap.aguiainova.ui.common.AoRetomar
import br.com.fiap.aguiainova.ui.common.Estado
import br.com.fiap.aguiainova.ui.common.viewModelDoApp
import br.com.fiap.aguiainova.ui.components.AbasDaTela
import br.com.fiap.aguiainova.ui.components.CartaoAguia
import br.com.fiap.aguiainova.ui.components.ConteudoDoEstado
import br.com.fiap.aguiainova.ui.components.EstadoVazio
import br.com.fiap.aguiainova.ui.components.LinhaQuebravel
import br.com.fiap.aguiainova.ui.components.Selo
import br.com.fiap.aguiainova.ui.components.TelaBase
import br.com.fiap.aguiainova.ui.ia.AssistenteViewModel
import br.com.fiap.aguiainova.ui.ia.ConteudoAssistente
import br.com.fiap.aguiainova.ui.theme.AguiaCores
import br.com.fiap.aguiainova.ui.theme.cor
import br.com.fiap.aguiainova.util.Formatadores

/** As partes da tela de estrategia; [argumento] e o que a navegacao usa para abrir direto numa delas. */
enum class AbaEstrategia(val argumento: String, val rotulo: String) {
    ASSISTENTE(Rotas.ABA_ASSISTENTE, "Assistente"),
    VIGENTES("vigentes", "Vigentes"),
    ENCERRADAS("encerradas", "Encerradas"),
    HISTORICO("historico", "Histórico")
}

/** O gestor ganha o assistente de IA como primeira aba; todos os perfis veem as demais. */
fun abasDe(perfil: Perfil): List<AbaEstrategia> =
    if (perfil == Perfil.GESTOR) {
        listOf(AbaEstrategia.ASSISTENTE, AbaEstrategia.VIGENTES, AbaEstrategia.ENCERRADAS, AbaEstrategia.HISTORICO)
    } else {
        listOf(AbaEstrategia.VIGENTES, AbaEstrategia.ENCERRADAS, AbaEstrategia.HISTORICO)
    }

/** Posicao da aba pedida pela navegacao; sem pedido (ou pedido invalido), abre nas estrategias vigentes. */
fun indiceInicial(abas: List<AbaEstrategia>, argumento: String?): Int =
    abas.indexOfFirst { it.argumento == argumento }.takeIf { it >= 0 } ?: abas.indexOf(AbaEstrategia.VIGENTES)

/**
 * Onde a empresa define e acompanha a sua direcao. Para a lideranca e o operador e "Estratégia"; para o gestor,
 * "Direção", com o assistente de IA junto das estrategias. Cada aba mostra uma parte do assunto sem sair da tela.
 */
@Composable
fun EstrategiasScreen(perfil: Perfil, navController: NavController, abaInicial: String? = null) {
    val vm = viewModelDoApp(chave = "estrategias-${perfil.name}") { EstrategiasViewModel(perfil, it.estrategias) }
    val historico = viewModelDoApp(chave = "estrategias-historico") { HistoricoEstrategiasViewModel(it.estrategias) }
    val assistente = viewModelDoApp(chave = "estrategias-assistente") { AssistenteViewModel(it.ia) }
    val estado by vm.estado.collectAsStateWithLifecycle()
    val avisoDaLista by vm.aviso.collectAsStateWithLifecycle()
    val avisoDoHistorico by historico.aviso.collectAsStateWithLifecycle()
    val avisoDoAssistente by assistente.aviso.collectAsStateWithLifecycle()
    AoRetomar(vm::carregar)

    val abas = remember(perfil) { abasDe(perfil) }
    var selecionada by rememberSaveable(abaInicial) { mutableStateOf(indiceInicial(abas, abaInicial)) }
    val aba = abas[selecionada.coerceIn(0, abas.lastIndex)]
    val lideranca = perfil == Perfil.LIDERANCA

    TelaBase(
        titulo = if (perfil == Perfil.GESTOR) "Direção" else "Estratégia",
        subtitulo = when (perfil) {
            Perfil.OPERADOR -> "O que a empresa quer alcançar: suas ideias valem mais quando ajudam nisso."
            Perfil.GESTOR -> "Converse com a IA e acompanhe se cada estratégia está andando."
            Perfil.LIDERANCA -> "Defina a direção e acompanhe se ela está funcionando."
        },
        navController = navController,
        perfil = perfil,
        aviso = avisoDaLista ?: avisoDoHistorico ?: avisoDoAssistente,
        aoAvisoLido = {
            vm.avisoLido()
            historico.avisoLido()
            assistente.avisoLido()
        },
        botaoFlutuante = {
            if (lideranca && (aba == AbaEstrategia.VIGENTES || aba == AbaEstrategia.ENCERRADAS)) {
                FloatingActionButton(
                    onClick = { navController.navigate(Rotas.estrategiaForm()) },
                    containerColor = AguiaCores.Primaria
                ) { Icon(Icons.Default.Add, contentDescription = "Nova estratégia") }
            }
        }
    ) { modificador ->
        Column(modificador) {
            AbasDaTela(abas.map { it.rotulo }, selecionada, { selecionada = it })
            val corpo = Modifier.weight(1f).fillMaxWidth()
            when (aba) {
                AbaEstrategia.ASSISTENTE -> ConteudoAssistente(assistente, corpo)
                AbaEstrategia.HISTORICO -> ConteudoHistorico(historico, corpo)
                AbaEstrategia.VIGENTES, AbaEstrategia.ENCERRADAS -> ListaDeEstrategias(
                    estado = estado,
                    vigentes = aba == AbaEstrategia.VIGENTES,
                    perfil = perfil,
                    aoTentarNovamente = vm::carregar,
                    aoAbrir = { navController.navigate(Rotas.estrategiaDetalhe(it.id)) },
                    aoCriar = { navController.navigate(Rotas.estrategiaForm()) },
                    modifier = corpo
                )
            }
        }
    }
}

@Composable
private fun ListaDeEstrategias(
    estado: Estado<PainelEstrategias>,
    vigentes: Boolean,
    perfil: Perfil,
    aoTentarNovamente: () -> Unit,
    aoAbrir: (Estrategia) -> Unit,
    aoCriar: () -> Unit,
    modifier: Modifier
) {
    ConteudoDoEstado(estado, aoTentarNovamente = aoTentarNovamente, modifier = modifier) { painel ->
        val lista = if (vigentes) painel.vigentes else painel.encerradas
        if (lista.isEmpty()) {
            SemEstrategias(vigentes, perfil, aoCriar)
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
            ) {
                if (vigentes && painel.saude.isNotEmpty()) {
                    item { ResumoDeSaude(painel.contagemPorNivel()) }
                }
                items(lista, key = { it.id }) { estrategia ->
                    CartaoEstrategia(estrategia, painel.saude[estrategia.id]) { aoAbrir(estrategia) }
                }
            }
        }
    }
}

@Composable
private fun SemEstrategias(vigentes: Boolean, perfil: Perfil, aoCriar: () -> Unit) {
    when {
        !vigentes -> EstadoVazio(
            "Nenhuma estratégia encerrada",
            "Quando uma estratégia for encerrada ela fica guardada aqui, para consulta."
        )
        perfil == Perfil.LIDERANCA -> EstadoVazio(
            "Nenhuma estratégia vigente",
            "Comece descrevendo o que a empresa quer alcançar: a IA escreve um rascunho com metas e período " +
                "para você revisar.",
            textoAcao = "Criar estratégia",
            aoAcionar = aoCriar
        )
        else -> EstadoVazio(
            "Nenhuma estratégia vigente ainda",
            "Quando a liderança publicar as diretrizes, elas aparecem aqui e ajudam a direcionar " +
                if (perfil == Perfil.GESTOR) "a avaliação das ideias." else "as suas ideias."
        )
    }
}

/** "2 em dia · 1 em risco": o retrato da carteira de estrategias antes de olhar uma a uma. */
@Composable
private fun ResumoDeSaude(contagem: Map<NivelSaude, Int>) {
    LinhaQuebravel {
        NivelSaude.entries.filter { (contagem[it] ?: 0) > 0 }.forEach { nivel ->
            Selo("${contagem[nivel]} ${nivel.rotulo.lowercase()}", nivel.cor())
        }
    }
}

@Composable
private fun CartaoEstrategia(estrategia: Estrategia, saude: SaudeEstrategia?, aoClicar: () -> Unit) {
    CartaoAguia(aoClicar = aoClicar) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                Text(
                    estrategia.titulo,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                saude?.let { Selo(it.nivel.rotulo, it.nivel.cor()) }
            }
            Text(
                estrategia.descricao,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            LinhaQuebravel {
                Selo(estrategia.categoria, AguiaCores.Primaria)
                estrategia.campanha?.let { Selo(it, AguiaCores.Alerta) }
            }
            resumoDoPlano(estrategia)?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            saude?.let {
                Text("Próxima ação: ${it.proximaAcao}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

/** "2 metas · 01/01/2026 a 31/12/2026", ou nulo quando a estrategia nao tem metas nem periodo. */
private fun resumoDoPlano(estrategia: Estrategia): String? {
    val metas = estrategia.metas.size.takeIf { it > 0 }?.let { if (it == 1) "1 meta" else "$it metas" }
    return listOfNotNull(metas, Formatadores.periodo(estrategia.inicio, estrategia.fim))
        .takeIf { it.isNotEmpty() }?.joinToString(" · ")
}
