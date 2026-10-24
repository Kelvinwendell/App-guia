package br.com.fiap.aguiainova.ui.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Lista com `key` fica ancorada no item que estava visivel: quando entra um item novo no topo (a ideia/projeto que
 * a pessoa acabou de criar, ja que a API ordena do mais recente), ele surge ACIMA da tela e parece que nao foi salvo.
 * Se a pessoa estava no topo, rola para mostrar o novo; se estava lendo mais abaixo, nao mexe na posicao dela.
 */
@Composable
fun MostrarNovoNoTopo(estado: LazyListState, primeiraChave: Any?) {
    var anterior by remember { mutableStateOf(primeiraChave) }
    LaunchedEffect(primeiraChave) {
        val entrouNovoNoTopo = anterior != null && primeiraChave != anterior
        if (entrouNovoNoTopo && estado.firstVisibleItemIndex <= 1) estado.scrollToItem(0)
        anterior = primeiraChave
    }
}
