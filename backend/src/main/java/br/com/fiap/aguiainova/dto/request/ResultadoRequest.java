package br.com.fiap.aguiainova.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResultadoRequest(
        /** Opcional; se omitida, usa a data de hoje. */
        LocalDate data,
        @NotBlank @Size(max = 500) String descricao,
        @NotNull @DecimalMin("0.0") BigDecimal retornoFinanceiro,
        @DecimalMin("-100.0") @DecimalMax("1000.0") Double ganhoProdutividade,
        @DecimalMin("-100.0") @DecimalMax("100.0") Double reducaoCustos) {
}
