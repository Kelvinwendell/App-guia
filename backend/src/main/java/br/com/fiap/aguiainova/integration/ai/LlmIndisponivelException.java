package br.com.fiap.aguiainova.integration.ai;

/** O modelo de linguagem nao pode atender ao pedido; quem chama decide se usa o plano de contingencia. */
public class LlmIndisponivelException extends RuntimeException {

    public LlmIndisponivelException(String mensagem) {
        super(mensagem);
    }

    public LlmIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
