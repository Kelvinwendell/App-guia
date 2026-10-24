package br.com.fiap.aguiainova.ui.projetos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.aguiainova.domain.model.AndamentoProjeto
import br.com.fiap.aguiainova.domain.model.DadosResultado
import br.com.fiap.aguiainova.domain.model.EtapaProjeto
import br.com.fiap.aguiainova.domain.model.Perfil
import br.com.fiap.aguiainova.domain.model.Projeto
import br.com.fiap.aguiainova.domain.model.ResultadoProjeto
import br.com.fiap.aguiainova.domain.model.StatusProjeto
import br.com.fiap.aguiainova.ui.common.AoRetomar
import br.com.fiap.aguiainova.ui.common.viewModelDoApp
import br.com.fiap.aguiainova.ui.components.BarraProgresso
import br.com.fiap.aguiainova.ui.components.BotaoPrimario
import br.com.fiap.aguiainova.ui.components.CampoTexto
import br.com.fiap.aguiainova.ui.components.CartaoAguia
import br.com.fiap.aguiainova.ui.components.ConteudoDoEstado
import br.com.fiap.aguiainova.ui.components.DialogoConfirmacao
import br.com.fiap.aguiainova.ui.components.LinhaInfo
import br.com.fiap.aguiainova.ui.components.Selo
import br.com.fiap.aguiainova.ui.components.Seletor
import br.com.fiap.aguiainova.ui.components.TelaBase
import br.com.fiap.aguiainova.ui.components.TituloSecao
import br.com.fiap.aguiainova.ui.theme.AguiaCores
import br.com.fiap.aguiainova.ui.theme.cor
import br.com.fiap.aguiainova.util.Formatadores
import kotlin.math.roundToInt

@Composable
fun ProjetoDetalheScreen(projetoId: String, perfil: Perfil, aoVoltar: () -> Unit) {
    val vm = viewModelDoApp(chave = "projeto-$projetoId") { ProjetoDetalheViewModel(projetoId, it.projetos) }
    val estado by vm.estado.collectAsStateWithLifecycle()
    val ocupado by vm.ocupado.collectAsStateWithLifecycle()
    val aviso by vm.aviso.collectAsStateWithLifecycle()
    AoRetomar(vm::carregar)
    val gestor = perfil == Perfil.GESTOR
    var registrandoResultado by remember { mutableStateOf(false) }
    var confirmandoExclusao by remember { mutableStateOf(false) }

    TelaBase(
        titulo = "Projeto",
        aoVoltar = aoVoltar,
        aviso = aviso,
        aoAvisoLido = vm::avisoLido
    ) { modificador ->
        ConteudoDoEstado(estado, aoTentarNovamente = vm::carregar, modifier = modificador) { projeto ->
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ResumoDoProjeto(projeto)
                IndicadoresDoProjeto(projeto)
                if (gestor) {
                    EditorDeAndamento(projeto, ocupado, vm::salvarAndamento)
                }
                TituloSecao("Resultados obtidos")
                if (projeto.resultados.isEmpty()) {
                    Text("Nenhum resultado registrado ainda.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                projeto.resultados.forEach { resultado ->
                    CartaoResultado(resultado, removivel = gestor) { vm.removerResultado(resultado.id) }
                }
                if (gestor) {
                    BotaoPrimario("Adicionar resultado", aoClicar = { registrandoResultado = true })
                    TextButton(onClick = { confirmandoExclusao = true }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text("Excluir projeto", color = AguiaCores.Erro)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (registrandoResultado) {
        DialogoResultado(
            aoConfirmar = { dados -> vm.adicionarResultado(dados); registrandoResultado = false },
            aoCampoInvalido = vm::avisarCampoInvalido,
            aoCancelar = { registrandoResultado = false }
        )
    }
    if (confirmandoExclusao) {
        DialogoConfirmacao(
            titulo = "Excluir projeto?",
            texto = "O projeto e todos os seus resultados serão removidos.",
            aoConfirmar = { confirmandoExclusao = false; vm.excluir(aoVoltar) },
            aoCancelar = { confirmandoExclusao = false }
        )
    }
}

@Composable
private fun ResumoDoProjeto(projeto: Projeto) {
    CartaoAguia {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(projeto.nome, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(projeto.descricao, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Selo(projeto.status.rotulo, projeto.status.cor())
                Selo(projeto.etapa.rotulo, MaterialTheme.colorScheme.onSurfaceVariant)
                if (projeto.atrasado) Selo("Atrasado", AguiaCores.Erro)
            }
            BarraProgresso(projeto.progresso)
            LinhaInfo("Progresso", "${projeto.progresso}%")
            LinhaInfo("Prazo", "${Formatadores.data(projeto.prazo)} (${textoDeDias(projeto.diasParaPrazo)})")
            projeto.estrategiaTitulo?.let { LinhaInfo("Estratégia", it) }
            projeto.ideiaTitulo?.let { LinhaInfo("Ideia de origem", it) }
            projeto.autorNome?.let { LinhaInfo("Cadastrado por", it) }
        }
    }
}

@Composable
private fun IndicadoresDoProjeto(projeto: Projeto) {
    CartaoAguia {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LinhaInfo("Investimento", Formatadores.moeda(projeto.investimento))
            LinhaInfo("Retorno financeiro", Formatadores.moeda(projeto.retornoFinanceiro))
            LinhaInfo(
                "Lucro", Formatadores.moeda(projeto.lucro),
                corValor = if (projeto.lucro < 0) AguiaCores.Erro else AguiaCores.Sucesso
            )
            LinhaInfo("ROI", Formatadores.percentual(projeto.roi))
            LinhaInfo("Aumento de produtividade", Formatadores.percentualComSinal(projeto.ganhoProdutividade))
            LinhaInfo("Redução de custos", Formatadores.percentual(projeto.reducaoCustos))
        }
    }
}

/** Controles do gestor para atualizar progresso, status e etapa. Reinicia sempre que o projeto e recarregado. */
@Composable
private fun EditorDeAndamento(projeto: Projeto, ocupado: Boolean, aoSalvar: (AndamentoProjeto) -> Unit) {
    var progresso by remember(projeto) { mutableFloatStateOf(projeto.progresso.toFloat()) }
    var status by remember(projeto) { mutableStateOf(projeto.status) }
    var etapa by remember(projeto) { mutableStateOf(projeto.etapa) }
    TituloSecao("Atualizar andamento")
    CartaoAguia {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Progresso: ${progresso.roundToInt()}%")
            Slider(value = progresso, onValueChange = { progresso = it }, valueRange = 0f..100f)
            Seletor("Status", StatusProjeto.entries, status, { it.rotulo }, { status = it ?: status })
            Seletor("Etapa", EtapaProjeto.entries, etapa, { it.rotulo }, { etapa = it ?: etapa })
            BotaoPrimario(
                "Salvar andamento",
                aoClicar = { aoSalvar(AndamentoProjeto(status, etapa, progresso.roundToInt())) },
                carregando = ocupado
            )
        }
    }
}

@Composable
private fun CartaoResultado(resultado: ResultadoProjeto, removivel: Boolean, aoRemover: () -> Unit) {
    CartaoAguia {
        Row(Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(resultado.descricao, fontWeight = FontWeight.SemiBold)
                Text(
                    "${Formatadores.data(resultado.data)} · ${Formatadores.moeda(resultado.retornoFinanceiro)}",
                    color = AguiaCores.Sucesso
                )
                val extras = listOfNotNull(
                    resultado.ganhoProdutividade?.let { "Produtividade ${Formatadores.percentualComSinal(it)}" },
                    resultado.reducaoCustos?.let { "Custos ${Formatadores.percentual(it)} menores" }
                )
                if (extras.isNotEmpty()) {
                    Text(extras.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (removivel) {
                IconButton(onClick = aoRemover) {
                    Icon(Icons.Default.Delete, contentDescription = "Remover resultado", tint = AguiaCores.Erro)
                }
            }
        }
    }
}

@Composable
private fun DialogoResultado(
    aoConfirmar: (DadosResultado) -> Unit,
    aoCampoInvalido: (String) -> Unit,
    aoCancelar: () -> Unit
) {
    var descricao by remember { mutableStateOf("") }
    var retorno by remember { mutableStateOf("") }
    var produtividade by remember { mutableStateOf("") }
    var custos by remember { mutableStateOf("") }
    var data by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = aoCancelar,
        title = { Text("Registrar resultado") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CampoTexto(descricao, { descricao = it }, "Descrição")
                CampoTexto(retorno, { retorno = it }, "Retorno financeiro (R$)", teclado = KeyboardType.Decimal)
                CampoTexto(produtividade, { produtividade = it }, "Aumento de produtividade (%) — opcional", teclado = KeyboardType.Decimal)
                CampoTexto(custos, { custos = it }, "Redução de custos (%) — opcional", teclado = KeyboardType.Decimal)
                CampoTexto(data, { data = it }, "Data dd/mm/aaaa (padrão: hoje)", teclado = KeyboardType.Number)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val valor = Formatadores.numeroDigitado(retorno)
                val dataIso = if (data.isBlank()) null else Formatadores.dataIsoDeDigitada(data)
                when {
                    descricao.isBlank() -> aoCampoInvalido("Descreva o resultado.")
                    valor == null || valor < 0 -> aoCampoInvalido("Informe um retorno financeiro válido.")
                    data.isNotBlank() && dataIso == null -> aoCampoInvalido("Informe a data no formato dd/mm/aaaa.")
                    else -> aoConfirmar(
                        DadosResultado(dataIso, descricao, valor, Formatadores.numeroDigitado(produtividade), Formatadores.numeroDigitado(custos))
                    )
                }
            }) { Text("Registrar") }
        },
        dismissButton = { TextButton(onClick = aoCancelar) { Text("Cancelar") } }
    )
}

private fun textoDeDias(dias: Long): String = when {
    dias > 0 -> "faltam $dias dia(s)"
    dias == 0L -> "vence hoje"
    else -> "${-dias} dia(s) de atraso"
}
