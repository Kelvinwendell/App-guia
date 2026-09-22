package br.com.fiap.aguiainova.exception;

import org.springframework.http.HttpStatus;

/** Dados bem formados que violam uma regra de negocio (ex.: prazo anterior ao inicio do projeto). */
public class RegraDeNegocioException extends ApiException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.UNPROCESSABLE_CONTENT;
    }
}
