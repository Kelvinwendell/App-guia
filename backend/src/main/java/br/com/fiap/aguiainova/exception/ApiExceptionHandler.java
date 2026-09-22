package br.com.fiap.aguiainova.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Converte qualquer erro em ProblemDetail (RFC 7807). O app le o campo {@code detail} para mostrar ao usuario;
 * erros de validacao trazem tambem {@code errors} (campo -> mensagem).
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    ProblemDetail tratarApi(ApiException ex) {
        return problema(ex.status(), ex.getMessage());
    }

    /** Negacoes lancadas pelo {@code @PreAuthorize} chegam aqui (as do filtro sao tratadas no SecurityConfig). */
    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail tratarAcessoNegado(AccessDeniedException ex) {
        return problema(HttpStatus.FORBIDDEN, "Seu perfil não tem permissão para esta operação.");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    ProblemDetail tratarDuplicidade(DuplicateKeyException ex) {
        return problema(HttpStatus.CONFLICT, "Já existe um registro com estes dados.");
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail tratarInesperado(Exception ex) {
        log.error("Erro inesperado", ex);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Tente novamente mais tarde.");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> erros = new LinkedHashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            erros.putIfAbsent(erro.getField(), erro.getDefaultMessage());
        }
        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Dados inválidos: " + resumo(erros));
        problema.setProperty("errors", erros);
        return handleExceptionInternal(ex, problema, headers, HttpStatus.BAD_REQUEST, request);
    }

    private static String resumo(Map<String, String> erros) {
        StringBuilder texto = new StringBuilder();
        erros.forEach((campo, mensagem) -> {
            if (!texto.isEmpty()) {
                texto.append("; ");
            }
            texto.append(campo).append(" ").append(mensagem);
        });
        return texto.toString();
    }

    private static ProblemDetail problema(HttpStatus status, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(status.getReasonPhrase());
        return problema;
    }
}
