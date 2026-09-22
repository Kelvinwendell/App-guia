package br.com.fiap.aguiainova.exception;

import org.springframework.http.HttpStatus;

/** O usuario esta autenticado, mas nao pode acessar aquele recurso (ex.: ideia de outro autor). */
public class ProibidoException extends ApiException {

    public ProibidoException(String mensagem) {
        super(mensagem);
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.FORBIDDEN;
    }
}
