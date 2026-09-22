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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.aguiainova.domain.model.MensagemChat
import br.com.fiap.aguiainova.domain.model.PapelMensagem
import br.com.fiap.aguiainova.ui.common.viewModelDoApp
import br.com.fiap.aguiainova.ui.components.CampoTexto
import br.com.fiap.aguiainova.ui.components.Selo
import br.com.fiap.aguiainova.ui.components.TelaBase
import br.com.fiap.aguiainova.ui.theme.AguiaCores

@Composable
fun AssistenteIaScreen(aoVoltar: () -> Unit) {
    val vm = viewModelDoApp { AssistenteViewModel(it.ia) }
    val mensagens by vm.mensagens.collectAsStateWithLifecycle()
    val status by vm.status.collectAsStateWithLifecycle()
    val ocupado by vm.ocupado.collectAsStateWithLifecycle()
    val aviso by vm.aviso.collectAsStateWithLifecycle()
    var texto by remember { mutableStateOf("") }
    val lista = rememberLazyListState()
    LaunchedEffect(Unit) { vm.carregarStatus() }
    LaunchedEffect(mensagens.size) { if (mensagens.isNotEmpty()) lista.animateScrollToItem(mensagens.lastIndex) }
    val disponivel = status?.disponivel != false

    TelaBase(
        titulo = "Assistente de IA",
        subtitulo = "Ajuda para escolher ideias e montar projetos.",
        aoVoltar = aoVoltar,
        aviso = aviso,
        aoAvisoLido = vm::avisoLido
    ) { modificador ->
        Column(modificador, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            status?.let {
                Selo(
                    if (it.disponivel) "IA conectada · ${it.provedor.lowercase()} (${it.modelo})" else "IA não configurada no servidor",
                    if (it.disponivel) AguiaCores.Sucesso else AguiaCores.Alerta
                )
            }
            LazyColumn(state = lista, modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (mensagens.isEmpty()) {
                    item {
                        Text(
                            "Pergunte, por exemplo: \"Quais ideias em análise têm mais a ver com as estratégias vigentes?\" " +
                                "ou \"Monte um projeto a partir da ideia de sensores\".",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                        tint = if (podeEnviar) AguiaCores.Primaria else AguiaCores.TextoSecundario.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun Balao(mensagem: MensagemChat) {
    val doUsuario = mensagem.papel == PapelMensagem.USUARIO
    Box(Modifier.fillMaxWidth(), contentAlignment = if (doUsuario) Alignment.CenterEnd else Alignment.CenterStart) {
        Text(
            mensagem.texto,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (doUsuario) AguiaCores.Primaria else MaterialTheme.colorScheme.surface)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}
