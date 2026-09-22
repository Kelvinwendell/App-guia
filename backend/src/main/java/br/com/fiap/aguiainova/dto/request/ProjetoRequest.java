package br.com.fiap.aguiainova.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.com.fiap.aguiainova.model.enums.EtapaProjeto;
import br.com.fiap.aguiainova.model.enums.StatusProjeto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProjetoRequest(
        @NotBlank @Size(max = 150) String nome,
        @NotBlank @Size(max = 2000) String descricao,
        @NotNull @DecimalMin("0.0") BigDecimal investimento,
        /** Opcional; se omitida, usa a data de hoje. */
        LocalDate dataInicio,
        @NotNull LocalDate prazo,
        /** Opcional: estratégia VIGENTE à qual o projeto responde. */
        String estrategiaId,
        /** Opcional: ideia APROVADA que originou o projeto. */
        String ideiaId,
        /** Opcional; padrão PLANEJAMENTO na criação e valor atual na atualização. */
        StatusProjeto status,
        /** Opcional; padrão INICIACAO na criação e valor atual na atualização. */
        EtapaProjeto etapa) {
}
