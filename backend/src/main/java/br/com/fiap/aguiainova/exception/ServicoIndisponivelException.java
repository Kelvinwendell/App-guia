package br.com.fiap.aguiainova.exception;

import org.springframework.http.HttpStatus;

/** Um servico do qual a operacao depende (ex.: IA generativa) nao esta disponivel. */
public class ServicoIndisponivelException extends ApiException {

    public ServicoIndisponivelException(String mensagem) {
        super(mensagem);
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.SERVICE_UNAVAILABLE;
    }
}
