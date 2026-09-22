package br.com.fiap.aguiainova.dto.response;

import java.math.BigDecimal;

/** Retorno agregado dos projetos (e quantidade de ideias) vinculados a uma estratégia. */
public record DesempenhoEstrategiaResponse(
        /** Nulo no agrupamento "Sem estratégia vinculada". */
        String estrategiaId,
        String titulo,
        String categoria,
        int projetos,
        long ideias,
        BigDecimal investimento,
        BigDecimal retorno,
        BigDecimal lucro,
        BigDecimal roiPercentual) {
}
