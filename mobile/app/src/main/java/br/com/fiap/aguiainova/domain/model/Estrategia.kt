package br.com.fiap.aguiainova.domain.model

data class Estrategia(
    val id: String,
    val titulo: String,
    val descricao: String,
    val categoria: String,
    val campanha: String? = null,
    val status: StatusEstrategia,
    /** Resultados esperados, de preferencia mensuraveis. */
    val metas: List<String> = emptyList(),
    /** yyyy-MM-dd; nulo quando o periodo nao foi definido. */
    val inicio: String? = null,
    val fim: String? = null,
    val autorNome: String? = null,
    val criadoEm: String? = null
) {
    val vigente: Boolean get() = status == StatusEstrategia.VIGENTE
}

/** Registro historico de uma mudanca em uma estrategia (id, data, categoria e campanha, mais o conteudo). */
data class EstrategiaHistorico(
    val id: String,
    val estrategiaId: String,
    val data: String,
    val categoria: String,
    val campanha: String? = null,
    val acao: AcaoEstrategia,
    val titulo: String,
    val descricao: String,
    val status: StatusEstrategia,
    val autorNome: String? = null
)

/**
 * Saude de uma estrategia vigente, apurada por regras no servidor a partir das ideias e dos projetos vinculados a ela
 * (a IA so explica o resultado).
 */
data class SaudeEstrategia(
    val estrategiaId: String,
    val titulo: String,
    val categoria: String? = null,
    val nivel: NivelSaude,
    val motivos: List<String> = emptyList(),
    val proximaAcao: String,
    val ideias: Long,
    val ideiasEmAnalise: Long,
    val aprovadasSemProjeto: Long,
    val projetos: Int,
    val projetosAtrasados: Int,
    val investimento: Double,
    val retorno: Double,
    val roiPercentual: Double? = null,
    /** yyyy-MM-dd; nulo quando nada esta vinculado a estrategia. */
    val ultimaAtividade: String? = null
)
