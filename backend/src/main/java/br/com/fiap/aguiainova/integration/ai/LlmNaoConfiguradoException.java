package br.com.fiap.aguiainova.integration.ai;

/**
 * Nao ha provedor de IA configurado (situacao esperada em ambientes sem chave de API). Especializa a
 * indisponibilidade para que a contingencia entre em acao sem registrar alarmes no log.
 */
public class LlmNaoConfiguradoException extends LlmIndisponivelException {

    public LlmNaoConfiguradoException() {
        super("IA não configurada: defina AI_API_KEY para habilitá-la.");
    }
}
