package br.com.fiap.aguiainova.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.com.fiap.aguiainova.exception.RegraDeNegocioException;
import br.com.fiap.aguiainova.util.Validacoes;

/** Resultado a registrar em um projeto, ja validado. */
public record DadosResultado(LocalDate data, String descricao, BigDecimal retornoFinanceiro,
        Double ganhoProdutividade, Double reducaoCustos) {

    public DadosResultado {
        data = Validacoes.exigir(data, "data");
        descricao = Validacoes.exigirTexto(descricao, "descrição");
        retornoFinanceiro = Validacoes.exigir(retornoFinanceiro, "retorno financeiro");
        if (retornoFinanceiro.signum() < 0) {
            throw new RegraDeNegocioException("O retorno financeiro não pode ser negativo.");
        }
    }
}
