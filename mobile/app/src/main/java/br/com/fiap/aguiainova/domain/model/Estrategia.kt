package br.com.fiap.aguiainova.domain.model

data class Estrategia(
    val id: String,
    val titulo: String,
    val descricao: String,
    val categoria: String,
    val campanha: String? = null,
    val status: StatusEstrategia,
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
