package br.com.fiap.aguiainova.domain.model

/** Pontuacao sugerida pela IA (ou pela avaliacao por regras): so orienta; quem decide e o gestor. */
data class AvaliacaoIa(
    val score: Int,
    val alinhamentoEstrategico: Int,
    val prioridadeSugerida: Prioridade,
    val justificativa: String,
    val pontosFortes: List<String> = emptyList(),
    val riscos: List<String> = emptyList(),
    /** GEMINI, OPENAI ou HEURISTICA (regras locais, quando nao ha IA disponivel). */
    val fonte: String
) {
    val geradaPorIa: Boolean get() = fonte != FONTE_REGRAS

    companion object {
        const val FONTE_REGRAS = "HEURISTICA"
    }
}

data class Ideia(
    val id: String,
    val titulo: String,
    val descricao: String,
    val categoria: String,
    val status: StatusIdeia,
    val prioridade: Prioridade,
    val autorId: String,
    val autorNome: String,
    val estrategiaId: String? = null,
    val estrategiaTitulo: String? = null,
    val parecer: String? = null,
    val avaliadorNome: String? = null,
    /** So vem preenchida para o GESTOR. */
    val avaliacaoIa: AvaliacaoIa? = null,
    val criadoEm: String? = null
) {
    /** O autor so pode editar ou excluir enquanto a ideia esta em analise. */
    val editavelPeloAutor: Boolean get() = status == StatusIdeia.EM_ANALISE
}
