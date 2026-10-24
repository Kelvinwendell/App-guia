package br.com.fiap.aguiainova.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import br.com.fiap.aguiainova.domain.model.NivelSaude
import br.com.fiap.aguiainova.domain.model.Prioridade
import br.com.fiap.aguiainova.domain.model.StatusEstrategia
import br.com.fiap.aguiainova.domain.model.StatusIdeia
import br.com.fiap.aguiainova.domain.model.StatusProjeto

/** Cor de cada valor do dominio, definida uma unica vez (antes cada tela repetia o seu proprio "when"). */
@Composable
fun StatusIdeia.cor(): Color = when (this) {
    StatusIdeia.APROVADA -> AguiaCores.Sucesso
    StatusIdeia.REJEITADA -> AguiaCores.Erro
    StatusIdeia.EM_ANALISE -> AguiaCores.Alerta
}

@Composable
fun Prioridade.cor(): Color = when (this) {
    Prioridade.ALTA -> AguiaCores.Erro
    Prioridade.MEDIA -> AguiaCores.Alerta
    Prioridade.BAIXA -> AguiaCores.Sucesso
}

@Composable
fun StatusEstrategia.cor(): Color = when (this) {
    StatusEstrategia.VIGENTE -> AguiaCores.Sucesso
    StatusEstrategia.ENCERRADA -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
fun NivelSaude.cor(): Color = when (this) {
    NivelSaude.EM_DIA -> AguiaCores.Sucesso
    NivelSaude.EM_RISCO -> AguiaCores.Erro
    NivelSaude.SEM_TRACAO -> AguiaCores.Alerta
}

@Composable
fun StatusProjeto.cor(): Color = when (this) {
    StatusProjeto.EM_ANDAMENTO -> AguiaCores.Primaria
    StatusProjeto.CONCLUIDO -> AguiaCores.Sucesso
    StatusProjeto.PLANEJAMENTO -> AguiaCores.Alerta
    StatusProjeto.PAUSADO -> MaterialTheme.colorScheme.onSurfaceVariant
    StatusProjeto.CANCELADO -> AguiaCores.Erro
}
