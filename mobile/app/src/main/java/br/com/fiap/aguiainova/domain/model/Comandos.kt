package br.com.fiap.aguiainova.domain.model

/*
 * Dados que o app envia para criar ou alterar recursos. Os nomes dos campos sao os da API (Gson usa o nome do
 * campo); campos nulos sao omitidos do JSON, e a API trata "ausente" como "nao informado".
 */

data class Credenciais(val email: String, val senha: String)

data class NovoUsuario(
    val nome: String,
    val email: String,
    val senha: String,
    val perfil: Perfil,
    val cargo: String? = null,
    val departamento: String? = null,
    val unidade: String? = null
)

data class DadosEstrategia(
    val titulo: String,
    val descricao: String,
    val categoria: String,
    val campanha: String? = null,
    val status: StatusEstrategia? = null,
    val metas: List<String> = emptyList(),
    /** yyyy-MM-dd */
    val inicio: String? = null,
    /** yyyy-MM-dd */
    val fim: String? = null
)

data class DadosIdeia(
    val titulo: String,
    val descricao: String,
    val categoria: String,
    val estrategiaId: String? = null
)

/** Decisao do gestor sobre uma ideia: informe status e/ou prioridade e/ou parecer. */
data class AvaliacaoIdeia(
    val status: StatusIdeia? = null,
    val prioridade: Prioridade? = null,
    val parecer: String? = null
)

data class DadosProjeto(
    val nome: String,
    val descricao: String,
    val investimento: Double,
    /** yyyy-MM-dd; se omitido, a API usa a data de hoje. */
    val dataInicio: String? = null,
    /** yyyy-MM-dd */
    val prazo: String,
    val estrategiaId: String? = null,
    val ideiaId: String? = null,
    val status: StatusProjeto? = null,
    val etapa: EtapaProjeto? = null
)

/** Atualizacao de andamento: os campos nulos permanecem como estao. */
data class AndamentoProjeto(
    val status: StatusProjeto? = null,
    val etapa: EtapaProjeto? = null,
    val progresso: Int? = null
)

data class DadosResultado(
    /** yyyy-MM-dd; se omitido, a API usa a data de hoje. */
    val data: String? = null,
    val descricao: String,
    val retornoFinanceiro: Double,
    val ganhoProdutividade: Double? = null,
    val reducaoCustos: Double? = null
)

data class PedidoChat(
    val mensagem: String,
    val historico: List<MensagemChat> = emptyList()
)

/** Objetivo escrito pela lideranca; a IA devolve um rascunho de estrategia. */
data class PedidoRascunhoEstrategia(val objetivo: String)

/** Estrategia a criticar antes de salvar; [estrategiaId] evita compara-la consigo mesma ao editar. */
data class PedidoAnaliseEstrategia(val estrategiaId: String?, val estrategia: DadosEstrategia)
