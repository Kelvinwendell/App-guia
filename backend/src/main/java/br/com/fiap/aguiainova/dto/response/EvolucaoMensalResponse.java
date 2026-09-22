package br.com.fiap.aguiainova.dto.response;

import java.math.BigDecimal;

public record EvolucaoMensalResponse(
        /** Mês no formato yyyy-MM. */
        String mes,
        BigDecimal retornoNoMes,
        BigDecimal retornoAcumulado) {
}
