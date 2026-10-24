package br.com.fiap.aguiainova.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Item criado agora (que a API devolve no topo) aparece na tela, em vez de surgir acima dela. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ListasTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun itemNovoNoTopoFicaVisivelQuandoAListaEstavaNoTopo() {
        val itens = mutableStateOf((1..20).map { "Item $it" })
        compose.setContent {
            val posicao = rememberLazyListState()
            MostrarNovoNoTopo(posicao, itens.value.first())
            LazyColumn(Modifier.height(300.dp), state = posicao) {
                items(itens.value, key = { it }) { Text(it, Modifier.height(60.dp)) }
            }
        }

        compose.runOnIdle { itens.value = listOf("Recém-criado") + itens.value }

        compose.onNodeWithText("Recém-criado").assertIsDisplayed()
    }
}
