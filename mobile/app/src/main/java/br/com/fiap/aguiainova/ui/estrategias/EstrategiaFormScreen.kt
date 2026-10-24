package br.com.fiap.aguiainova.ui.estrategias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.aguiainova.domain.model.AnaliseEstrategia
import br.com.fiap.aguiainova.domain.model.DadosEstrategia
import br.com.fiap.aguiainova.domain.model.StatusEstrategia
import br.com.fiap.aguiainova.ui.common.Estado
import br.com.fiap.aguiainova.ui.common.viewModelDoApp
import br.com.fiap.aguiainova.ui.components.BotaoPrimario
import br.com.fiap.aguiainova.ui.components.CampoTexto
import br.com.fiap.aguiainova.ui.components.CartaoAguia
import br.com.fiap.aguiainova.ui.components.ConteudoDoEstado
import br.com.fiap.aguiainova.ui.components.Selo
import br.com.fiap.aguiainova.ui.components.Seletor
import br.com.fiap.aguiainova.ui.components.TelaBase
import br.com.fiap.aguiainova.ui.components.TituloSecao
import br.com.fiap.aguiainova.ui.theme.AguiaCores
import br.com.fiap.aguiainova.util.Formatadores

@Composable
fun EstrategiaFormScreen(estrategiaId: String?, aoConcluir: () -> Unit) {
    val vm = viewModelDoApp(chave = "estrategia-form-${estrategiaId ?: "nova"}") {
        EstrategiaFormViewModel(estrategiaId, it.estrategias, it.ia)
    }
    val estado by vm.estado.collectAsStateWithLifecycle()
    val analise by vm.analise.collectAsStateWithLifecycle()
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
            var objetivo by remember { mutableStateOf("") }
            var titulo by remember { mutableStateOf(existente?.titulo.orEmpty()) }
            var descricao by remember { mutableStateOf(existente?.descricao.orEmpty()) }
            var categoria by remember { mutableStateOf(existente?.categoria.orEmpty()) }
            var campanha by remember { mutableStateOf(existente?.campanha.orEmpty()) }
            var status by remember { mutableStateOf(existente?.status ?: StatusEstrategia.VIGENTE) }
            val metas = remember { mutableStateListOf<String>().apply { addAll(existente?.metas.orEmpty()) } }
            var inicio by remember { mutableStateOf(existente?.inicio?.let { Formatadores.data(it) }.orEmpty()) }
            var fim by remember { mutableStateOf(existente?.fim?.let { Formatadores.data(it) }.orEmpty()) }

            fun dadosDoFormulario(): DadosEstrategia? = montarDados(
                titulo, descricao, categoria, campanha, status, metas, inicio, fim, vm::avisarCampoInvalido
            )

            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (estrategiaId == null) {
                    RascunhoComIa(objetivo, { objetivo = it }, ocupado) {
                        vm.rascunhar(objetivo) { rascunho ->
                            titulo = rascunho.titulo
                            descricao = rascunho.descricao
                            categoria = rascunho.categoria
                            campanha = rascunho.campanha.orEmpty()
                            metas.clear()
                            metas.addAll(rascunho.metas)
                            inicio = rascunho.inicio?.let { Formatadores.data(it) }.orEmpty()
                            fim = rascunho.fim?.let { Formatadores.data(it) }.orEmpty()
                        }
                    }
                }
                CampoTexto(titulo, { titulo = it }, "Título")
                CampoTexto(descricao, { descricao = it }, "Descrição", linhas = 4)
                CampoTexto(categoria, { categoria = it }, "Categoria")
                CampoTexto(campanha, { campanha = it }, "Campanha (opcional)")
                Seletor("Status", StatusEstrategia.entries, status, { it.rotulo }, { status = it ?: status })

                TituloSecao("Metas", Modifier.padding(top = 4.dp))
                Text(
                    "O que precisa acontecer para a estratégia dar certo? Prefira metas com número e prazo, " +
                        "como \"Reduzir o desperdício em 15% até dezembro\".",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                metas.forEachIndexed { posicao, meta ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CampoTexto(meta, { metas[posicao] = it }, "Meta ${posicao + 1}", Modifier.weight(1f))
                        IconButton(onClick = { metas.removeAt(posicao) }) {
                            Icon(Icons.Default.Close, contentDescription = "Remover meta ${posicao + 1}")
                        }
                    }
                }
                if (metas.size < LIMITE_DE_METAS) {
                    OutlinedButton(onClick = { metas.add("") }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text("  Adicionar meta")
                    }
                }

                TituloSecao("Período de vigência", Modifier.padding(top = 4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CampoTexto(inicio, { inicio = it }, "Início", Modifier.weight(1f), teclado = KeyboardType.Text, dica = "dd/mm/aaaa")
                    CampoTexto(fim, { fim = it }, "Fim", Modifier.weight(1f), teclado = KeyboardType.Text, dica = "dd/mm/aaaa")
                }

                AnaliseComIa(analise, aoAnalisar = { dadosDoFormulario()?.let(vm::analisar) })
                BotaoPrimario(
                    "Salvar estratégia",
                    aoClicar = { dadosDoFormulario()?.let { vm.salvar(it, aoConcluir) } },
                    carregando = ocupado
                )
            }
        }
    }
}

/**
 * Converte o que foi digitado em [DadosEstrategia]. Metas em branco sao descartadas; as datas, se preenchidas, precisam
 * ser dd/mm/aaaa e o fim nao pode ser anterior ao inicio. Em caso de erro avisa e devolve nulo.
 */
private fun montarDados(
    titulo: String,
    descricao: String,
    categoria: String,
    campanha: String,
    status: StatusEstrategia,
    metas: List<String>,
    inicio: String,
    fim: String,
    avisar: (String) -> Unit
): DadosEstrategia? {
    val inicioIso = inicio.takeIf { it.isNotBlank() }?.let {
        Formatadores.dataIsoDeDigitada(it) ?: return avisarDataInvalida("início", avisar)
    }
    val fimIso = fim.takeIf { it.isNotBlank() }?.let {
        Formatadores.dataIsoDeDigitada(it) ?: return avisarDataInvalida("fim", avisar)
    }
    if (inicioIso != null && fimIso != null && fimIso < inicioIso) {
        avisar("O fim da estratégia não pode ser anterior ao início.")
        return null
    }
    return DadosEstrategia(
        titulo = titulo.trim(),
        descricao = descricao.trim(),
        categoria = categoria.trim(),
        campanha = campanha.trim().ifBlank { null },
        status = status,
        metas = metas.map { it.trim() }.filter { it.isNotEmpty() },
        inicio = inicioIso,
        fim = fimIso
    )
}

private fun avisarDataInvalida(campo: String, avisar: (String) -> Unit): DadosEstrategia? {
    avisar("Informe o $campo no formato dd/mm/aaaa.")
    return null
}

/** A lideranca descreve o objetivo com as proprias palavras; a IA escreve o rascunho e ela revisa antes de salvar. */
@Composable
private fun RascunhoComIa(objetivo: String, aoMudar: (String) -> Unit, ocupado: Boolean, aoGerar: () -> Unit) {
    CartaoAguia {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AguiaCores.Primaria)
                Text("Escrever com IA", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Text(
                "Conte o que a empresa quer alcançar. A IA monta um rascunho com título, metas e período; " +
                    "você revisa e ajusta antes de salvar.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            CampoTexto(objetivo, aoMudar, "Objetivo", linhas = 3, dica = "Ex.: reduzir o desperdício nas linhas de produção")
            BotaoPrimario("Gerar rascunho", aoClicar = aoGerar, carregando = ocupado, habilitado = objetivo.isNotBlank())
        }
    }
}

/** A IA critica a estrategia antes de ser salva (ou, sem IA, as regras): clareza, metas mensuraveis, periodo e sobreposicao. */
@Composable
private fun AnaliseComIa(estado: Estado<AnaliseEstrategia>?, aoAnalisar: () -> Unit) {
    CartaoAguia {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            when (estado) {
                null -> Text(
                    "Antes de salvar, peça uma crítica: clareza, metas mensuráveis, período e sobreposição com " +
                        "outras estratégias.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Estado.Carregando -> CircularProgressIndicator()
                is Estado.Erro -> Text(estado.mensagem, color = AguiaCores.Erro)
                is Estado.Pronto -> ConteudoDaAnalise(estado.dados)
            }
            OutlinedButton(onClick = aoAnalisar, enabled = estado != Estado.Carregando) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Text(if (estado == null) "  Analisar com IA" else "  Analisar novamente")
            }
        }
    }
}

@Composable
private fun ConteudoDaAnalise(analise: AnaliseEstrategia) {
    val corDaNota = when {
        analise.nota >= NOTA_BOA -> AguiaCores.Sucesso
        analise.nota >= NOTA_REGULAR -> AguiaCores.Alerta
        else -> AguiaCores.Erro
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Selo("Nota ${analise.nota}/100", corDaNota)
        Selo(if (analise.geradaPorIa) "IA · ${analise.fonte.lowercase()}" else "Regras", AguiaCores.TextoSecundario)
    }
    Text(analise.resumo, fontWeight = FontWeight.SemiBold)
    if (analise.pontosFortes.isNotEmpty()) {
        Text("Pontos fortes", fontWeight = FontWeight.Bold, color = AguiaCores.Sucesso)
        analise.pontosFortes.forEach { Text("✓ $it") }
    }
    if (analise.melhorias.isNotEmpty()) {
        Text("O que melhorar", fontWeight = FontWeight.Bold, color = AguiaCores.Alerta)
        analise.melhorias.forEach { Text("• $it") }
    }
    if (analise.sobreposicoes.isNotEmpty()) {
        Text(
            "Parecida com: " + analise.sobreposicoes.joinToString { it.titulo ?: "outra estratégia" },
            color = AguiaCores.Alerta,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private const val LIMITE_DE_METAS = 8
private const val NOTA_BOA = 70
private const val NOTA_REGULAR = 40
