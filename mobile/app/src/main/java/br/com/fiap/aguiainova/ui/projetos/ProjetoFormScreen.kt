package br.com.fiap.aguiainova.ui.projetos

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.aguiainova.domain.model.DadosProjeto
import br.com.fiap.aguiainova.ui.common.viewModelDoApp
import br.com.fiap.aguiainova.ui.components.BotaoPrimario
import br.com.fiap.aguiainova.ui.components.CampoTexto
import br.com.fiap.aguiainova.ui.components.ConteudoDoEstado
import br.com.fiap.aguiainova.ui.components.Seletor
import br.com.fiap.aguiainova.ui.components.TelaBase
import br.com.fiap.aguiainova.util.Formatadores

@Composable
fun ProjetoFormScreen(aoConcluir: () -> Unit) {
    val vm = viewModelDoApp { ProjetoFormViewModel(it.projetos, it.estrategias, it.ideias) }
    val estado by vm.estado.collectAsStateWithLifecycle()
    val ocupado by vm.ocupado.collectAsStateWithLifecycle()
    val aviso by vm.aviso.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.carregar() }

    TelaBase(
        titulo = "Novo projeto",
        subtitulo = "Cadastre uma iniciativa e vincule à estratégia vigente.",
        aoVoltar = aoConcluir,
        aviso = aviso,
        aoAvisoLido = vm::avisoLido
    ) { modificador ->
        ConteudoDoEstado(estado, aoTentarNovamente = vm::carregar, modifier = modificador) { opcoes ->
            var nome by remember { mutableStateOf("") }
            var descricao by remember { mutableStateOf("") }
            var investimento by remember { mutableStateOf("") }
            var prazo by remember { mutableStateOf("") }
            var estrategiaId by remember { mutableStateOf<String?>(null) }
            var ideiaId by remember { mutableStateOf<String?>(null) }

            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoTexto(nome, { nome = it }, "Nome do projeto")
                CampoTexto(descricao, { descricao = it }, "Descrição", linhas = 3)
                CampoTexto(investimento, { investimento = it }, "Investimento (R$)", teclado = KeyboardType.Decimal)
                CampoTexto(prazo, { prazo = it }, "Prazo (dd/mm/aaaa)", teclado = KeyboardType.Number)
                Seletor(
                    rotulo = "Estratégia vigente",
                    opcoes = opcoes.estrategiasVigentes,
                    selecionado = opcoes.estrategiasVigentes.firstOrNull { it.id == estrategiaId },
                    rotuloDe = { it.titulo },
                    aoSelecionar = { estrategiaId = it?.id },
                    textoVazio = "Nenhuma"
                )
                Seletor(
                    rotulo = "Ideia aprovada de origem",
                    opcoes = opcoes.ideiasAprovadas,
                    selecionado = opcoes.ideiasAprovadas.firstOrNull { it.id == ideiaId },
                    rotuloDe = { it.titulo },
                    aoSelecionar = { ideiaId = it?.id },
                    textoVazio = "Nenhuma"
                )
                BotaoPrimario(
                    "Cadastrar projeto",
                    aoClicar = {
                        val valor = Formatadores.numeroDigitado(investimento)
                        val prazoIso = Formatadores.dataIsoDeDigitada(prazo)
                        when {
                            nome.isBlank() || descricao.isBlank() -> vm.avisarCampoInvalido("Preencha nome e descrição.")
                            valor == null || valor < 0 -> vm.avisarCampoInvalido("Informe um investimento válido.")
                            prazoIso == null -> vm.avisarCampoInvalido("Informe o prazo no formato dd/mm/aaaa.")
                            else -> vm.criar(
                                DadosProjeto(nome, descricao, valor, prazo = prazoIso, estrategiaId = estrategiaId, ideiaId = ideiaId)
                            ) { aoConcluir() }
                        }
                    },
                    carregando = ocupado
                )
            }
        }
    }
}
