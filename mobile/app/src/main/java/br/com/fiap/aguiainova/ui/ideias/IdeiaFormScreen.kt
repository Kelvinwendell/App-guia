package br.com.fiap.aguiainova.ui.ideias

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
import br.com.fiap.aguiainova.domain.model.DadosIdeia
import br.com.fiap.aguiainova.ui.common.viewModelDoApp
import br.com.fiap.aguiainova.ui.components.BotaoPrimario
import br.com.fiap.aguiainova.ui.components.CampoTexto
import br.com.fiap.aguiainova.ui.components.ConteudoDoEstado
import br.com.fiap.aguiainova.ui.components.Seletor
import br.com.fiap.aguiainova.ui.components.TelaBase

@Composable
fun IdeiaFormScreen(ideiaId: String?, aoConcluir: () -> Unit) {
    val vm = viewModelDoApp(chave = "ideia-form-${ideiaId ?: "nova"}") { IdeiaFormViewModel(ideiaId, it.ideias, it.estrategias) }
    val estado by vm.estado.collectAsStateWithLifecycle()
    val ocupado by vm.ocupado.collectAsStateWithLifecycle()
    val aviso by vm.aviso.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.carregar() }

    TelaBase(
        titulo = if (ideiaId == null) "Nova ideia" else "Editar ideia",
        aoVoltar = aoConcluir,
        aviso = aviso,
        aoAvisoLido = vm::avisoLido
    ) { modificador ->
        ConteudoDoEstado(estado, aoTentarNovamente = vm::carregar, modifier = modificador) { formulario ->
            val existente = formulario.existente
            var titulo by remember { mutableStateOf(existente?.titulo.orEmpty()) }
            var descricao by remember { mutableStateOf(existente?.descricao.orEmpty()) }
            var categoria by remember { mutableStateOf(existente?.categoria.orEmpty()) }
            var estrategiaId by remember { mutableStateOf(existente?.estrategiaId) }
            var tentouSalvar by remember { mutableStateOf(false) }

            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoTexto(
                    titulo, { titulo = it }, "Título",
                    erro = if (tentouSalvar && titulo.isBlank()) "Informe um título." else null
                )
                CampoTexto(
                    descricao, { descricao = it }, "Descreva sua ideia", linhas = 4,
                    erro = if (tentouSalvar && descricao.isBlank()) "Descreva a ideia." else null
                )
                CampoTexto(
                    categoria, { categoria = it }, "Categoria",
                    erro = if (tentouSalvar && categoria.isBlank()) "Informe uma categoria." else null
                )
                Seletor(
                    rotulo = "Estratégia relacionada",
                    opcoes = formulario.estrategiasVigentes,
                    selecionado = formulario.estrategiasVigentes.firstOrNull { it.id == estrategiaId },
                    rotuloDe = { it.titulo },
                    aoSelecionar = { estrategiaId = it?.id },
                    textoVazio = "Nenhuma"
                )
                BotaoPrimario(
                    "Salvar ideia",
                    aoClicar = {
                        tentouSalvar = true
                        vm.salvar(DadosIdeia(titulo, descricao, categoria, estrategiaId), aoConcluir)
                    },
                    carregando = ocupado
                )
            }
        }
    }
}
