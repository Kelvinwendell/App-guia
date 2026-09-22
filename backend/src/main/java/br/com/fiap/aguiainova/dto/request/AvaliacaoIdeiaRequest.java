package br.com.fiap.aguiainova.dto.request;

import br.com.fiap.aguiainova.model.enums.Prioridade;
import br.com.fiap.aguiainova.model.enums.StatusIdeia;
import jakarta.validation.constraints.Size;

/** Decisão do gestor sobre uma ideia. Informe {@code status} e/ou {@code prioridade}. */
public record AvaliacaoIdeiaRequest(
        StatusIdeia status,
        Prioridade prioridade,
        @Size(max = 1000) String parecer) {
}
