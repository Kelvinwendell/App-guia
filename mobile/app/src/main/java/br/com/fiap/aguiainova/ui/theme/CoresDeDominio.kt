package br.com.fiap.aguiainova.ui.theme

import androidx.compose.ui.graphics.Color
import br.com.fiap.aguiainova.domain.model.Prioridade
import br.com.fiap.aguiainova.domain.model.StatusEstrategia
import br.com.fiap.aguiainova.domain.model.StatusIdeia
import br.com.fiap.aguiainova.domain.model.StatusProjeto

/** Cor de cada valor do dominio, definida uma unica vez (antes cada tela repetia o seu proprio "when"). */
fun StatusIdeia.cor(): Color = when (this) {
    StatusIdeia.APROVADA -> AguiaCores.Sucesso
    StatusIdeia.REJEITADA -> AguiaCores.Erro
    StatusIdeia.EM_ANALISE -> AguiaCores.Alerta
}

fun Prioridade.cor(): Color = when (this) {
    Prioridade.ALTA -> AguiaCores.Erro
    Prioridade.MEDIA -> AguiaCores.Alerta
    Prioridade.BAIXA -> AguiaCores.Sucesso
}

fun StatusEstrategia.cor(): Color = when (this) {
    StatusEstrategia.VIGENTE -> AguiaCores.Sucesso
    StatusEstrategia.ENCERRADA -> AguiaCores.TextoSecundario
}

fun StatusProjeto.cor(): Color = when (this) {
    StatusProjeto.EM_ANDAMENTO -> AguiaCores.Primaria
    StatusProjeto.CONCLUIDO -> AguiaCores.Sucesso
    StatusProjeto.PLANEJAMENTO -> AguiaCores.Alerta
    StatusProjeto.PAUSADO -> AguiaCores.TextoSecundario
    StatusProjeto.CANCELADO -> AguiaCores.Erro
}
