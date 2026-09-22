package br.com.fiap.aguiainova.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.function.Function;

/**
 * Indicadores derivados de um projeto. Fonte unica das formulas: usada tanto na resposta da API de projetos
 * quanto nos relatorios do dashboard, para que os numeros nunca divirjam.
 *
 * <ul>
 *   <li>retorno = soma do retorno financeiro de todos os resultados registrados</li>
 *   <li>lucro = retorno - investimento</li>
 *   <li>ROI (%) = lucro / investimento x 100 (nulo quando nao ha investimento)</li>
 *   <li>produtividade e reducao de custos = media dos resultados que informaram o valor</li>
 * </ul>
 */
public record ProjetoMetricas(
        BigDecimal retornoFinanceiro,
        BigDecimal lucro,
        BigDecimal roi,
        Double ganhoProdutividade,
        Double reducaoCustos) {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);
    private static final int CASAS_DECIMAIS = 2;

    public static ProjetoMetricas de(Projeto projeto) {
        List<ResultadoProjeto> resultados = projeto.getResultados() == null ? List.of() : projeto.getResultados();
        BigDecimal retorno = resultados.stream()
                .map(ResultadoProjeto::retornoFinanceiro)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal investimento = projeto.getInvestimento() == null ? BigDecimal.ZERO : projeto.getInvestimento();
        BigDecimal lucro = retorno.subtract(investimento);
        return new ProjetoMetricas(retorno, lucro, roi(lucro, investimento),
                mediaArredondada(resultados, ResultadoProjeto::ganhoProdutividade),
                mediaArredondada(resultados, ResultadoProjeto::reducaoCustos));
    }

    /** ROI em percentual; nulo se nao houve investimento (a divisao nao faz sentido). */
    public static BigDecimal roi(BigDecimal lucro, BigDecimal investimento) {
        if (investimento == null || investimento.signum() == 0) {
            return null;
        }
        return lucro.multiply(CEM).divide(investimento, CASAS_DECIMAIS, RoundingMode.HALF_UP);
    }

    /** Media dos valores informados (ignora nulos), com 2 casas; nulo quando nenhum item informou o valor. */
    public static <T> Double mediaArredondada(Collection<T> itens, Function<T, Double> campo) {
        OptionalDouble media = itens.stream()
                .map(campo)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average();
        if (media.isEmpty()) {
            return null;
        }
        return BigDecimal.valueOf(media.getAsDouble()).setScale(CASAS_DECIMAIS, RoundingMode.HALF_UP).doubleValue();
    }
}
