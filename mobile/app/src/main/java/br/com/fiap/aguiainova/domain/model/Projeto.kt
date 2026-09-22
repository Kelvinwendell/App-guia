package br.com.fiap.aguiainova.domain.model

data class ResultadoProjeto(
    val id: String,
    val data: String,
    val descricao: String,
    val retornoFinanceiro: Double,
    val ganhoProdutividade: Double? = null,
    val reducaoCustos: Double? = null,
    val registradoPorNome: String? = null
)

data class Projeto(
    val id: String,
    val nome: String,
    val descricao: String,
    val status: StatusProjeto,
    val etapa: EtapaProjeto,
    val investimento: Double,
    val dataInicio: String? = null,
    val prazo: String,
    val progresso: Int,
    val estrategiaId: String? = null,
    val estrategiaTitulo: String? = null,
    val ideiaId: String? = null,
    val ideiaTitulo: String? = null,
    val autorNome: String? = null,
    val retornoFinanceiro: Double = 0.0,
    val lucro: Double = 0.0,
    /** ROI em %, nulo quando nao ha investimento. */
    val roi: Double? = null,
    val ganhoProdutividade: Double? = null,
    val reducaoCustos: Double? = null,
    val atrasado: Boolean = false,
    val diasParaPrazo: Long = 0,
    val resultados: List<ResultadoProjeto> = emptyList()
) {
    val ativo: Boolean get() = status == StatusProjeto.EM_ANDAMENTO
}
