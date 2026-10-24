package br.com.fiap.aguiainova.domain.model

/**
 * Enumeracoes do dominio. Os nomes das constantes sao exatamente os valores da API (o Gson converte pelo nome);
 * cada uma sabe o proprio rotulo em portugues, entao nenhuma tela compara ou monta texto de status por conta propria.
 */
enum class Perfil(val rotulo: String) {
    OPERADOR("Operador"),
    GESTOR("Gestor"),
    LIDERANCA("Liderança");

    companion object {
        fun deApi(valor: String?): Perfil? = entries.firstOrNull { it.name.equals(valor, ignoreCase = true) }
    }
}

enum class StatusIdeia(val rotulo: String) {
    EM_ANALISE("Em análise"),
    APROVADA("Aprovada"),
    REJEITADA("Rejeitada")
}

enum class Prioridade(val rotulo: String) {
    BAIXA("Baixa"),
    MEDIA("Média"),
    ALTA("Alta")
}

enum class StatusEstrategia(val rotulo: String) {
    VIGENTE("Vigente"),
    ENCERRADA("Encerrada")
}

enum class NivelSaude(val rotulo: String) {
    EM_DIA("Em dia"),
    EM_RISCO("Em risco"),
    SEM_TRACAO("Sem tração")
}

enum class AcaoEstrategia(val rotulo: String) {
    CRIADA("Criada"),
    ATUALIZADA("Atualizada"),
    REMOVIDA("Removida")
}

enum class StatusProjeto(val rotulo: String, val emAberto: Boolean) {
    PLANEJAMENTO("Planejamento", true),
    EM_ANDAMENTO("Em andamento", true),
    CONCLUIDO("Concluído", false),
    PAUSADO("Pausado", true),
    CANCELADO("Cancelado", false)
}

enum class EtapaProjeto(val rotulo: String) {
    INICIACAO("Iniciação"),
    PLANEJAMENTO("Planejamento"),
    EXECUCAO("Execução"),
    MONITORAMENTO("Monitoramento"),
    ENCERRAMENTO("Encerramento")
}

enum class PapelMensagem {
    USUARIO,
    ASSISTENTE
}
