package br.com.fiap.aguiainova.ui.ia

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    var texto by rememberSaveable { mutableStateOf("") }
    val lista = rememberLazyListState()
    LaunchedEffect(Unit) { vm.carregarStatus() }
    LaunchedEffect(mensagens.size) { if (mensagens.isNotEmpty()) lista.animateScrollToItem(mensagens.lastIndex) }
    val disponivel = status?.disponivel != false

    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        status?.let {
            Selo(
                if (it.disponivel) "IA conectada · ${it.provedor.lowercase()} (${it.modelo})" else "IA não configurada no servidor",
                if (it.disponivel) AguiaCores.Sucesso else AguiaCores.Alerta
            )
        }
        LazyColumn(state = lista, modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (mensagens.isEmpty()) {
                item { Sugestoes(habilitadas = disponivel && !ocupado, aoEscolher = vm::enviar) }
            }
            items(mensagens) { Balao(it) }
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

@Composable
private fun Balao(mensagem: MensagemChat) {
    val doUsuario = mensagem.papel == PapelMensagem.USUARIO
    Box(Modifier.fillMaxWidth(), contentAlignment = if (doUsuario) Alignment.CenterEnd else Alignment.CenterStart) {
        Text(
            mensagem.texto,
            // texto deve usar o par on* do tema: onPrimary (escuro) sobre o azul claro da Primaria,
            // nao a cor de texto padrao (clara) - senao fica ilegivel sobre o balao do usuario.
            color = if (doUsuario) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (doUsuario) AguiaCores.Primaria else MaterialTheme.colorScheme.surface)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}

private val PERGUNTAS_SUGERIDAS = listOf(
    "Qual estratégia está mais em risco e o que devo fazer primeiro?",
    "Quais ideias em análise têm mais a ver com as estratégias vigentes?",
    "Monte um projeto a partir da ideia aprovada mais alinhada."
)
