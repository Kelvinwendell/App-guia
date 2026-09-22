package br.com.fiap.aguiainova.service.ia;

import java.util.List;

/** @param fonte GEMINI, OPENAI ou HEURISTICA */
public record InsightsGerados(String resumo, List<String> insights, List<String> recomendacoes, String fonte) {
}
