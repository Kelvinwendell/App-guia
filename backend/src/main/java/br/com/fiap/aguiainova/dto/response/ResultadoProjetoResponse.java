package br.com.fiap.aguiainova.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResultadoProjetoResponse(
        String id,
        LocalDate data,
        String descricao,
        BigDecimal retornoFinanceiro,
        Double ganhoProdutividade,
        Double reducaoCustos,
        String registradoPorNome) {
}
