package br.com.fiap.aguiainova.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Task
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.fiap.aguiainova.domain.model.Perfil
import br.com.fiap.aguiainova.ui.components.CartaoAcao
import br.com.fiap.aguiainova.ui.components.CartaoIndicador
import br.com.fiap.aguiainova.ui.components.TituloSecao
import br.com.fiap.aguiainova.ui.theme.AguiaCores
import br.com.fiap.aguiainova.util.Formatadores

/**
 * Conteudo puro das homes (SPEC-RD-004): um indicador compacto e exatamente uma acao primaria
 * (destaque=true) por perfil, conforme docs/redesign/specs/004-homes-por-perfil.md. Nao conhece
 * NavController nem Rotas - so emite os callbacks que a tela liga a navegacao.
 */
@Composable
fun HomeConteudo(
    perfil: Perfil,
    resumo: ResumoHome,
    aoAbrirIdeias: () -> Unit,
    aoAbrirEstrategias: () -> Unit,
    aoAbrirProjetos: () -> Unit,
    aoCriarProjeto: () -> Unit,
    aoAbrirAssistente: () -> Unit,
    aoAbrirDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (perfil) {
        Perfil.OPERADOR -> ConteudoOperador(resumo, aoAbrirIdeias, aoAbrirEstrategias, modifier)
        Perfil.GESTOR -> ConteudoGestor(resumo, aoAbrirIdeias, aoAbrirAssistente, aoCriarProjeto, aoAbrirProjetos, aoAbrirEstrategias, modifier)
        Perfil.LIDERANCA -> ConteudoLideranca(resumo, aoAbrirDashboard, aoAbrirEstrategias, aoAbrirProjetos, modifier)
    }
}

@Composable
private fun ConteudoOperador(resumo: ResumoHome, aoAbrirIdeias: () -> Unit, aoAbrirEstrategias: () -> Unit, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CartaoIndicador("Minhas ideias", resumo.ideiasTotal.toString(), AguiaCores.Primaria, Modifier.weight(1f))
            CartaoIndicador("Aprovadas", resumo.ideiasAprovadas.toString(), AguiaCores.Sucesso, Modifier.weight(1f))
        }
        TituloSecao("Ações rápidas")
        CartaoAcao("Minhas ideias", "Cadastre e acompanhe o status", Icons.Outlined.Lightbulb, destaque = true, aoClicar = aoAbrirIdeias)
        CartaoAcao(
            "Estratégia da empresa", "Veja o que a empresa quer alcançar e alinhe suas ideias",
            Icons.Outlined.Flag, aoClicar = aoAbrirEstrategias
        )
    }
}

@Composable
private fun ConteudoGestor(
    resumo: ResumoHome,
    aoAbrirIdeias: () -> Unit,
    aoAbrirAssistente: () -> Unit,
    aoCriarProjeto: () -> Unit,
    aoAbrirProjetos: () -> Unit,
    aoAbrirEstrategias: () -> Unit,
    modifier: Modifier
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CartaoIndicador("Ideias pendentes", resumo.ideiasPendentes.toString(), AguiaCores.Primaria, Modifier.weight(1f))
            CartaoIndicador("Projetos ativos", resumo.projetosAtivos.toString(), AguiaCores.Sucesso, Modifier.weight(1f))
        }
        CartaoIndicador("Retorno parcial", Formatadores.moeda(resumo.retornoParcial), AguiaCores.Alerta)
        TituloSecao("Ações rápidas")
        CartaoAcao("Aprovar ideias", "Analise, priorize e aprove ideias enviadas", Icons.Outlined.Lightbulb, destaque = true, aoClicar = aoAbrirIdeias)
        CartaoAcao("Assistente de IA", "Converse para escolher ideias e montar projetos", Icons.Outlined.AutoAwesome, aoClicar = aoAbrirAssistente)
        CartaoAcao("Criar projeto", "Cadastre novas iniciativas", Icons.Outlined.Work, aoClicar = aoCriarProjeto)
        CartaoAcao("Atualizar progresso", "Acompanhe indicadores e entregas", Icons.Outlined.Task, aoClicar = aoAbrirProjetos)
        CartaoAcao("Saúde das estratégias", "Veja quais estão em dia, em risco ou paradas", Icons.Outlined.Flag, aoClicar = aoAbrirEstrategias)
    }
}

@Composable
private fun ConteudoLideranca(
    resumo: ResumoHome,
    aoAbrirDashboard: () -> Unit,
    aoAbrirEstrategias: () -> Unit,
    aoAbrirProjetos: () -> Unit,
    modifier: Modifier
) {
    val painel = resumo.dashboard
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CartaoIndicador("ROI", Formatadores.percentual(painel?.roiPercentual), AguiaCores.Sucesso, Modifier.weight(1f))
            CartaoIndicador(
                "Custos", Formatadores.percentualComSinal(painel?.reducaoCustosMedia?.unaryMinus()),
                AguiaCores.Alerta, Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CartaoIndicador(
                "Produtividade", Formatadores.percentualComSinal(painel?.ganhoProdutividadeMedio),
                AguiaCores.Primaria, Modifier.weight(1f)
            )
            CartaoIndicador(
                "Projetos", (painel?.totalProjetos ?: 0).toString(),
                MaterialTheme.colorScheme.onSurfaceVariant, Modifier.weight(1f)
            )
        }
        TituloSecao("Ações rápidas")
        CartaoAcao("Painel executivo", "ROI, lucro, prazos e insights de IA", Icons.Outlined.BarChart, destaque = true, aoClicar = aoAbrirDashboard)
        CartaoAcao("Estratégia", "Crie com a IA, acompanhe a saúde e revise as diretrizes", Icons.Outlined.Flag, aoClicar = aoAbrirEstrategias)
        CartaoAcao(
            "Projetos estratégicos", "Acompanhe etapa, status, investimento e retorno",
            Icons.Outlined.Work, aoClicar = aoAbrirProjetos
        )
    }
}
