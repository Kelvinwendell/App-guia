package br.com.fiap.aguiainova.dto.response;

import java.time.Instant;
import java.util.List;

/**
 * Crítica de uma estratégia antes de publicada.
 *
 * @param nota  maturidade de 0 a 100
 * @param fonte GEMINI, OPENAI ou HEURISTICA (regras, quando a IA está indisponível)
 */
public record AnaliseEstrategiaResponse(
        int nota,
        String resumo,
        List<String> pontosFortes,
        List<String> melhorias,
        List<Sobreposicao> sobreposicoes,
        String fonte,
        Instant geradaEm) {

    /** Estratégia vigente que cobre um objetivo parecido com o da analisada. */
    public record Sobreposicao(String estrategiaId, String titulo) {
    }
}
