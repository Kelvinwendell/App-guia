package br.com.fiap.aguiainova.ui.ia

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import br.com.fiap.aguiainova.domain.model.StatusIa
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.aguiainova.domain.model.MensagemChat
import br.com.fiap.aguiainova.domain.model.PapelMensagem
import br.com.fiap.aguiainova.ui.components.CampoTexto
import br.com.fiap.aguiainova.ui.components.Selo
import br.com.fiap.aguiainova.ui.theme.AguiaCores

/**
 * Chat do gestor com a IA, para usar dentro de uma aba (a aba "Assistente" de Direção). O [vm] pertence a tela que
 * hospeda as abas, entao a conversa continua ao trocar de aba.
 */
@Composable
fun ConteudoAssistente(vm: AssistenteViewModel, modifier: Modifier = Modifier) {
    val mensagens by vm.mensagens.collectAsStateWithLifecycle()
    val status by vm.status.collectAsStateWithLifecycle()
    val ocupado by vm.ocupado.collectAsStateWithLifecycle()
    val statusVerificado by vm.statusVerificado.collectAsStateWithLifecycle()
    val porRegras by vm.respostasPorRegras.collectAsStateWithLifecycle()
    var texto by rememberSaveable { mutableStateOf("") }
    val lista = rememberLazyListState()
    LaunchedEffect(Unit) { vm.carregarStatus() }
    LaunchedEffect(mensagens.size) { if (mensagens.isNotEmpty()) lista.animateScrollToItem(mensagens.lastIndex) }
    val disponivel = status?.disponivel != false

    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SeloDeStatus(status, statusVerificado)
        LazyColumn(state = lista, modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (mensagens.isEmpty()) {
                item {
                    // Sem provedor de IA as perguntas prontas ficariam desabilitadas sob um "toque em uma": explica o porque.
                    if (status?.disponivel == false) SemIaConfigurada()
                    else Sugestoes(habilitadas = disponivel && !ocupado, aoEscolher = vm::enviar)
                }
            }
            itemsIndexed(mensagens) { posicao, mensagem -> Balao(mensagem, porRegras = posicao in porRegras) }
            if (ocupado) item { CircularProgressIndicator(Modifier.padding(8.dp)) }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            CampoTexto(texto, { texto = it }, "Sua pergunta", Modifier.weight(1f), habilitado = disponivel && !ocupado)
            val podeEnviar = disponivel && !ocupado && texto.isNotBlank()
            IconButton(onClick = { vm.enviar(texto); texto = "" }, enabled = podeEnviar) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enviar",
                    tint = if (podeEnviar) AguiaCores.Primaria else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
internal fun SemIaConfigurada() {
    Text(
        "O assistente precisa de um provedor de IA configurado no servidor. Enquanto isso, a avaliação das ideias e " +
            "os insights do painel continuam funcionando por regras.",
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/** Perguntas prontas: quem nao sabe o que perguntar toca em uma e ja ve a IA trabalhando com os dados da empresa. */
@Composable
private fun Sugestoes(habilitadas: Boolean, aoEscolher: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Peça ajuda para decidir onde focar. Toque em uma pergunta ou escreva a sua:",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        PERGUNTAS_SUGERIDAS.forEach { pergunta ->
            OutlinedButton(
                onClick = { aoEscolher(pergunta) },
                enabled = habilitadas,
                modifier = Modifier.fillMaxWidth()
            ) { Text(pergunta, textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth()) }
        }
    }
}

/** Enquanto o status nao chega nada e dito como "conectado"; se a consulta falhar, o selo diz que nao deu para saber. */
@Composable
internal fun SeloDeStatus(status: StatusIa?, verificado: Boolean) {
    when {
        status?.disponivel == true -> Selo("IA conectada · ${status.provedor.lowercase()} (${status.modelo})", AguiaCores.Sucesso)
        status != null -> Selo("IA não configurada no servidor", AguiaCores.Alerta)
        verificado -> Selo("Não foi possível verificar a IA", MaterialTheme.colorScheme.onSurfaceVariant)
        else -> Selo("Verificando a IA…", MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * Uma mensagem. Quem falou e dito pelo alinhamento, pela cor e tambem pela semantica ("Você:"/"Assistente:"),
 * para o leitor de tela. O texto nao tem limite de linhas: resposta longa e lida inteira rolando a conversa.
 */
@Composable
internal fun Balao(mensagem: MensagemChat, porRegras: Boolean = false) {
    val doUsuario = mensagem.papel == PapelMensagem.USUARIO
    val cores = MaterialTheme.colorScheme
    Column(
        Modifier.fillMaxWidth().semantics(mergeDescendants = true) {
            contentDescription = (if (doUsuario) "Você: " else "Assistente: ") + mensagem.texto +
                if (porRegras) " (leitura por regras, sem IA generativa)" else ""
        },
        horizontalAlignment = if (doUsuario) Alignment.End else Alignment.Start
    ) {
        Text(
            mensagem.texto,
            // Par on* do tema: onPrimary (escuro) sobre o azul da primaria; a cor padrao (clara) ficava ilegivel.
            color = if (doUsuario) cores.onPrimary else cores.onSurface,
            modifier = Modifier
                .fillMaxWidth(LARGURA_MAXIMA_DO_BALAO)
                .wrapContentWidth(if (doUsuario) Alignment.End else Alignment.Start)
                .clip(RoundedCornerShape(16.dp))
                .background(if (doUsuario) cores.primary else cores.surfaceContainer)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        )
        if (porRegras) {
            Text(
                "Leitura por regras (IA indisponível)",
                style = MaterialTheme.typography.labelSmall,
                color = cores.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, start = 4.dp)
            )
        }
    }
}

private const val LARGURA_MAXIMA_DO_BALAO = 0.85f

private val PERGUNTAS_SUGERIDAS = listOf(
    "Qual estratégia está mais em risco e o que devo fazer primeiro?",
    "Quais ideias em análise têm mais a ver com as estratégias vigentes?",
    "Monte um projeto a partir da ideia aprovada mais alinhada."
)
