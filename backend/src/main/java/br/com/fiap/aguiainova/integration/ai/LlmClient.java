package br.com.fiap.aguiainova.integration.ai;

/**
 * Porta de saida para um modelo de linguagem externo. O restante da aplicacao depende apenas desta interface,
 * entao trocar de provedor (Gemini, OpenRouter, GitHub Models...) e questao de configuracao.
 */
public interface LlmClient {

    /** Identificador do provedor (GEMINI, OPENAI ou NENHUM), registrado junto de cada resultado gerado. */
    String provedor();

    String modelo();

    /** Falso quando a IA esta desabilitada ou sem chave de API. */
    boolean disponivel();

    /**
     * Gera a resposta do modelo.
     *
     * @throws LlmIndisponivelException se o provedor nao esta configurado, falha ou nao devolve texto utilizavel
     */
    String gerar(PedidoLlm pedido);
}
