package br.com.fiap.aguiainova.exception;

import org.springframework.http.HttpStatus;

public class NaoEncontradoException extends ApiException {

    public NaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.NOT_FOUND;
    }
}
