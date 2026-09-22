package br.com.fiap.aguiainova.exception;

import org.springframework.http.HttpStatus;

/**
 * Raiz das falhas esperadas da aplicacao. Cada subclasse sabe qual status HTTP representa (polimorfismo), entao o
 * tratador de erros converte qualquer uma delas sem precisar conhecer os tipos concretos.
 */
public abstract class ApiException extends RuntimeException {

    protected ApiException(String mensagem) {
        super(mensagem);
    }

    public abstract HttpStatus status();
}
