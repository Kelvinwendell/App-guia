package br.com.fiap.aguiainova.exception;

import org.springframework.http.HttpStatus;

/** Muitas tentativas em pouco tempo para a mesma chave (login, registro ou IA). */
public class LimiteDeRequisicoesExcedidoException extends ApiException {

    public LimiteDeRequisicoesExcedidoException(String mensagem) {
        super(mensagem);
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.TOO_MANY_REQUESTS;
    }
}
