package br.com.fiap.aguiainova.dto.response;

import java.time.Instant;
import java.util.List;

/**
 * Leitura da saúde de uma estratégia: o diagnóstico calculado por regras e a explicação da IA (ou das regras).
 *
 * @param fonte GEMINI, OPENAI ou HEURISTICA
 */
public record LeituraSaudeResponse(
        SaudeEstrategiaResponse saude,
        String leitura,
        List<String> recomendacoes,
        String fonte,
        Instant geradaEm) {
}
