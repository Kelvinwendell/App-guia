package br.com.fiap.aguiainova.model;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

/**
 * Base de todo documento persistido: identificador e auditoria de criacao/alteracao, preenchidos pelo Spring
 * Data. Os campos nao tem setter; ninguem, fora da infraestrutura, altera essas informacoes.
 */
public abstract class DocumentoBase {

    @Id
    private String id;
    @CreatedDate
    private Instant criadoEm;
    @LastModifiedDate
    private Instant atualizadoEm;

    public String getId() {
        return id;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
