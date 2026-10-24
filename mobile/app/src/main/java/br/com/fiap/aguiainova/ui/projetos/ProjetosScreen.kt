package br.com.fiap.aguiainova.ui.projetos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import br.com.fiap.aguiainova.domain.model.Perfil
import br.com.fiap.aguiainova.domain.model.Projeto
import br.com.fiap.aguiainova.navigation.Rotas
import br.com.fiap.aguiainova.ui.common.AoRetomar
import br.com.fiap.aguiainova.ui.common.viewModelDoApp
import br.com.fiap.aguiainova.ui.components.BarraProgresso
import br.com.fiap.aguiainova.ui.components.CartaoAguia
import br.com.fiap.aguiainova.ui.components.ConteudoDoEstado
import br.com.fiap.aguiainova.ui.components.LinhaInfo
import br.com.fiap.aguiainova.ui.components.MensagemVazia
import br.com.fiap.aguiainova.ui.components.Selo
import br.com.fiap.aguiainova.ui.components.TelaBase
import br.com.fiap.aguiainova.ui.theme.AguiaCores
import br.com.fiap.aguiainova.ui.theme.cor
import br.com.fiap.aguiainova.util.Formatadores

@Composable
fun ProjetosScreen(perfil: Perfil, navController: NavController) {
    val vm = viewModelDoApp(chave = "projetos-${perfil.name}") { ProjetosViewModel(it.projetos) }
    val estado by vm.estado.collectAsStateWithLifecycle()
    val aviso by vm.aviso.collectAsStateWithLifecycle()
    AoRetomar(vm::carregar)
    val gestor = perfil == Perfil.GESTOR

    TelaBase(
        titulo = if (gestor) "Projetos" else "Projetos Estratégicos",
        subtitulo = if (gestor) "Cadastre projetos e atualize o progresso." else "Acompanhe etapa, status, investimento e retorno.",
        navController = navController,
        perfil = perfil,
        aviso = aviso,
        aoAvisoLido = vm::avisoLido,
        botaoFlutuante = {
            if (gestor) {
                FloatingActionButton(onClick = { navController.navigate(Rotas.PROJETO_NOVO) }, containerColor = AguiaCores.Primaria) {
                    Icon(Icons.Outlined.Add, contentDescription = "Novo projeto")
                }
            }
        }
    ) { modificador ->
        ConteudoDoEstado(estado, aoTentarNovamente = vm::carregar, modifier = modificador) { lista ->
            if (lista.isEmpty()) {
                MensagemVazia("Nenhum projeto cadastrado.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
                    items(lista, key = { it.id }) { projeto ->
                        CartaoProjeto(projeto) { navController.navigate(Rotas.projetoDetalhe(projeto.id)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartaoProjeto(projeto: Projeto, aoClicar: () -> Unit) {
    CartaoAguia(aoClicar = aoClicar) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(projeto.nome, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(projeto.descricao, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Selo(projeto.status.rotulo, projeto.status.cor())
                Selo(projeto.etapa.rotulo, MaterialTheme.colorScheme.onSurfaceVariant)
                if (projeto.atrasado) Selo("Atrasado", AguiaCores.Erro)
            }
            BarraProgresso(projeto.progresso)
            LinhaInfo("Progresso", "${projeto.progresso}%")
            LinhaInfo("Investimento", Formatadores.moeda(projeto.investimento))
            LinhaInfo("Retorno financeiro", Formatadores.moeda(projeto.retornoFinanceiro))
            LinhaInfo("Prazo", Formatadores.data(projeto.prazo))
        }
    }
}
