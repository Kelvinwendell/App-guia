package br.com.fiap.aguiainova.dto.request;

import br.com.fiap.aguiainova.model.enums.EtapaProjeto;
import br.com.fiap.aguiainova.model.enums.StatusProjeto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/** Atualização de andamento. Informe ao menos um campo; os omitidos permanecem como estão. */
public record ProgressoRequest(
        StatusProjeto status,
        EtapaProjeto etapa,
        @Min(0) @Max(100) Integer progresso) {
}
