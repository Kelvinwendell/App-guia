package br.com.fiap.aguiainova.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Resultado obtido por um projeto em determinado momento. Imutavel: para corrigir, remove-se e registra-se outro.
 *
 * @param retornoFinanceiro  valor gerado neste marco (o retorno do projeto e a soma dos marcos)
 * @param ganhoProdutividade aumento de produtividade em % (opcional)
 * @param reducaoCustos      reducao de custos em % (opcional)
 */
public record ResultadoProjeto(
        String id,
        LocalDate data,
        String descricao,
        BigDecimal retornoFinanceiro,
        Double ganhoProdutividade,
        Double reducaoCustos,
        Autor registradoPor) {
}
