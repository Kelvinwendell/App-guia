package br.com.fiap.aguiainova.exception;

import org.springframework.http.HttpStatus;

public class NaoAutenticadoException extends ApiException {

    public NaoAutenticadoException(String mensagem) {
        super(mensagem);
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.UNAUTHORIZED;
    }
}
