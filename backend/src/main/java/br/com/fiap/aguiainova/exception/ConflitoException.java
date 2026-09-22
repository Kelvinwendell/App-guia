package br.com.fiap.aguiainova.exception;

import org.springframework.http.HttpStatus;

/** O estado atual do recurso nao permite a operacao (ex.: e-mail ja cadastrado, ideia ja avaliada). */
public class ConflitoException extends ApiException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.CONFLICT;
    }
}
