package br.com.fiap.aguiainova.dto.response;

import java.time.Instant;
import java.util.List;

public record InsightsIaResponse(
        String resumo,
        List<String> insights,
        List<String> recomendacoes,
        /** GEMINI, OPENAI ou HEURISTICA (regras locais, quando a IA não está disponível). */
        String fonte,
        Instant geradoEm) {
}
