package br.com.fiap.aguiainova.dto.response;

import java.util.List;

public record RankingIaResponse(
        /** Ideias EM_ANALISE ordenadas da maior para a menor pontuação. */
        List<AvaliacaoIaResponse> ranking,
        /** Quantas ideias foram pontuadas nesta chamada (as demais já tinham pontuação). */
        int avaliadasAgora) {
}
