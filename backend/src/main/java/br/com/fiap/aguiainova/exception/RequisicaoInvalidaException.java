package br.com.fiap.aguiainova.exception;

import org.springframework.http.HttpStatus;

/** A requisicao esta incompleta ou incoerente (ex.: nenhum campo a alterar foi informado). */
public class RequisicaoInvalidaException extends ApiException {

    public RequisicaoInvalidaException(String mensagem) {
        super(mensagem);
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.BAD_REQUEST;
    }
}
