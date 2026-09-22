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
