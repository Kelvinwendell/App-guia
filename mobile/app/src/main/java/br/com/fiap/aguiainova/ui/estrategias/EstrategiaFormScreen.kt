package br.com.fiap.aguiainova.ui.estrategias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.aguiainova.domain.model.DadosEstrategia
import br.com.fiap.aguiainova.domain.model.StatusEstrategia
import br.com.fiap.aguiainova.ui.common.viewModelDoApp
import br.com.fiap.aguiainova.ui.components.BotaoPrimario
import br.com.fiap.aguiainova.ui.components.CampoTexto
import br.com.fiap.aguiainova.ui.components.ConteudoDoEstado
import br.com.fiap.aguiainova.ui.components.Seletor
import br.com.fiap.aguiainova.ui.components.TelaBase

@Composable
fun EstrategiaFormScreen(estrategiaId: String?, aoConcluir: () -> Unit) {
    val vm = viewModelDoApp(chave = "estrategia-form-${estrategiaId ?: "nova"}") { EstrategiaFormViewModel(estrategiaId, it.estrategias) }
    val estado by vm.estado.collectAsStateWithLifecycle()
    val ocupado by vm.ocupado.collectAsStateWithLifecycle()
    val aviso by vm.aviso.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.carregar() }

    TelaBase(
        titulo = if (estrategiaId == null) "Nova estratégia" else "Editar estratégia",
        aoVoltar = aoConcluir,
        aviso = aviso,
        aoAvisoLido = vm::avisoLido
    ) { modificador ->
        ConteudoDoEstado(estado, aoTentarNovamente = vm::carregar, modifier = modificador) { existente ->
            var titulo by remember { mutableStateOf(existente?.titulo.orEmpty()) }
            var descricao by remember { mutableStateOf(existente?.descricao.orEmpty()) }
            var categoria by remember { mutableStateOf(existente?.categoria.orEmpty()) }
            var campanha by remember { mutableStateOf(existente?.campanha.orEmpty()) }
            var status by remember { mutableStateOf(existente?.status ?: StatusEstrategia.VIGENTE) }

            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoTexto(titulo, { titulo = it }, "Título")
                CampoTexto(descricao, { descricao = it }, "Descrição", linhas = 4)
                CampoTexto(categoria, { categoria = it }, "Categoria")
                CampoTexto(campanha, { campanha = it }, "Campanha (opcional)")
                Seletor("Status", StatusEstrategia.entries, status, { it.rotulo }, { status = it ?: status })
                BotaoPrimario(
                    "Salvar estratégia",
                    aoClicar = {
                        vm.salvar(DadosEstrategia(titulo, descricao, categoria, campanha.ifBlank { null }, status), aoConcluir)
                    },
                    carregando = ocupado
                )
            }
        }
    }
}
