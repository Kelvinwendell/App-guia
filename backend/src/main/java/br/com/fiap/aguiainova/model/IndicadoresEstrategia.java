package br.com.fiap.aguiainova.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Retrato numerico de uma estrategia vigente: o que esta vinculado a ela e como anda. E a entrada das regras de
 * {@link SaudeEstrategia} e do texto que a IA recebe, entao os numeros que a pessoa ve e os que o modelo le sao os
 * mesmos. Projetos cancelados nao entram na contagem nem nos valores.
 *
 * @param criadaEm            dia em que a estrategia foi cadastrada
 * @param metas               quantidade de metas cadastradas
 * @param aprovadasSemProjeto ideias aprovadas desta estrategia para as quais ainda nao existe projeto
 * @param roiPercentual       nulo quando nao ha investimento
 * @param ultimaAtividade     ultima movimentacao de ideias, projetos ou resultados; nulo se nao ha vinculos
 */
public record IndicadoresEstrategia(
        LocalDate criadaEm,
        LocalDate inicio,
        LocalDate fim,
        int metas,
        long ideias,
        long ideiasEmAnalise,
        long aprovadasSemProjeto,
        int projetos,
        int projetosEmAberto,
        int projetosAtrasados,
        int projetosConcluidos,
        BigDecimal investimento,
        BigDecimal retorno,
        BigDecimal roiPercentual,
        LocalDate ultimaAtividade) {
}
