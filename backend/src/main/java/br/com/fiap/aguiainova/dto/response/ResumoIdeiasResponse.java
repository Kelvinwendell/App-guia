package br.com.fiap.aguiainova.dto.response;

import java.math.BigDecimal;

public record ResumoIdeiasResponse(
        long total,
        long emAnalise,
        long aprovadas,
        long rejeitadas,
        /** Aprovadas / (aprovadas + rejeitadas), em %; nulo enquanto nenhuma ideia foi decidida. */
        BigDecimal taxaAprovacaoPercentual) {
}
