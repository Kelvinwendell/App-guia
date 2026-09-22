package br.com.fiap.aguiainova.dto.request;

import br.com.fiap.aguiainova.model.enums.StatusEstrategia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EstrategiaRequest(
        @NotBlank @Size(max = 150) String titulo,
        @NotBlank @Size(max = 2000) String descricao,
        @NotBlank @Size(max = 80) String categoria,
        @Size(max = 120) String campanha,
        /** Opcional; se omitido, uma estratégia nova nasce VIGENTE e uma existente mantém o status. */
        StatusEstrategia status) {
}
