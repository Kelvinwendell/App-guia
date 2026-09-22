package br.com.fiap.aguiainova.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IdeiaRequest(
        @NotBlank @Size(max = 150) String titulo,
        @NotBlank @Size(max = 2000) String descricao,
        @NotBlank @Size(max = 80) String categoria,
        /** Opcional: id de uma estratégia VIGENTE à qual a ideia se relaciona. */
        String estrategiaId) {
}
