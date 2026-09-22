package br.com.fiap.aguiainova.domain.model

data class ResumoIdeias(
    val total: Long,
    val emAnalise: Long,
    val aprovadas: Long,
    val rejeitadas: Long,
    val taxaAprovacaoPercentual: Double? = null
)

data class PrazoProjeto(
    val projetoId: String,
    val nome: String,
    val status: StatusProjeto,
    val progresso: Int,
    val prazo: String,
    val diasParaPrazo: Long,
    val atrasado: Boolean
)

/** Visao geral para a lideranca (indicadores financeiros consideram os projetos nao cancelados). */
data class ResumoDashboard(
    val investimentoTotal: Double,
    val retornoTotal: Double,
    val lucroTotal: Double,
    val roiPercentual: Double? = null,
    val ganhoProdutividadeMedio: Double? = null,
    val reducaoCustosMedia: Double? = null,
    val totalProjetos: Int,
    val projetosAtrasados: Int,
    /** Chaves: nomes de [StatusProjeto]. */
    val projetosPorStatus: Map<String, Long> = emptyMap(),
    /** Chaves: nomes de [EtapaProjeto]. */
    val projetosPorEtapa: Map<String, Long> = emptyMap(),
    val ideias: ResumoIdeias,
    val proximosPrazos: List<PrazoProjeto> = emptyList()
)

data class DesempenhoEstrategia(
    /** Nulo no grupo "Sem estrategia vinculada". */
    val estrategiaId: String? = null,
    val titulo: String,
    val categoria: String? = null,
    val projetos: Int,
    val ideias: Long,
    val investimento: Double,
    val retorno: Double,
    val lucro: Double,
    val roiPercentual: Double? = null
)

data class DesempenhoProjeto(
    val projetoId: String,
    val nome: String,
    val status: StatusProjeto,
    val etapa: EtapaProjeto,
    val progresso: Int,
    val prazo: String,
    val atrasado: Boolean,
    val investimento: Double,
    val retorno: Double,
    val lucro: Double,
    val roiPercentual: Double? = null,
    val ganhoProdutividade: Double? = null,
    val reducaoCustos: Double? = null
)

data class EvolucaoMensal(
    /** yyyy-MM */
    val mes: String,
    val retornoNoMes: Double,
    val retornoAcumulado: Double
)
