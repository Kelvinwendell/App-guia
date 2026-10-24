package br.com.fiap.aguiainova.domain.model

data class StatusIa(
    val habilitada: Boolean,
    /** Verdadeiro quando ha provedor de IA configurado; senao so a avaliacao por regras funciona. */
    val disponivel: Boolean,
    val provedor: String,
    val modelo: String
)

data class AvaliacaoIdeiaIa(
    val ideiaId: String,
    val titulo: String,
    val autorNome: String? = null,
    val status: StatusIdeia,
    val prioridadeAtual: Prioridade,
    val avaliacao: AvaliacaoIa
)

data class RankingIa(
    val ranking: List<AvaliacaoIdeiaIa>,
    val avaliadasAgora: Int
)

data class MensagemChat(
    val papel: PapelMensagem,
    val texto: String
)

data class RespostaChat(
    val resposta: String,
    val fonte: String
)

data class InsightsIa(
    val resumo: String,
    val insights: List<String> = emptyList(),
    val recomendacoes: List<String> = emptyList(),
    val fonte: String,
    val geradoEm: String? = null
)

/** Rascunho de estrategia escrito pela IA; a lideranca revisa antes de salvar. */
data class RascunhoEstrategia(
    val titulo: String,
    val descricao: String,
    val categoria: String,
    val campanha: String? = null,
    val metas: List<String> = emptyList(),
    val inicio: String? = null,
    val fim: String? = null,
    val fonte: String
)

data class SobreposicaoEstrategia(val estrategiaId: String, val titulo: String? = null)

/** Critica de uma estrategia antes de publicada (nota de 0 a 100, pontos fortes, melhorias e sobreposicao). */
data class AnaliseEstrategia(
    val nota: Int,
    val resumo: String,
    val pontosFortes: List<String> = emptyList(),
    val melhorias: List<String> = emptyList(),
    val sobreposicoes: List<SobreposicaoEstrategia> = emptyList(),
    /** GEMINI, OPENAI ou HEURISTICA (regras locais, quando a IA esta indisponivel). */
    val fonte: String,
    val geradaEm: String? = null
) {
    val geradaPorIa: Boolean get() = fonte != AvaliacaoIa.FONTE_REGRAS
}

/** Explicacao da saude de uma estrategia: o diagnostico por regras mais a leitura da IA (ou das regras). */
data class LeituraSaude(
    val saude: SaudeEstrategia,
    val leitura: String,
    val recomendacoes: List<String> = emptyList(),
    val fonte: String,
    val geradaEm: String? = null
) {
    val geradaPorIa: Boolean get() = fonte != AvaliacaoIa.FONTE_REGRAS
}
