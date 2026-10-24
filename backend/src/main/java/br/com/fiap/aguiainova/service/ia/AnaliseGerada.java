package br.com.fiap.aguiainova.service.ia;

import java.util.List;

/**
 * Critica de uma estrategia antes de ser publicada.
 *
 * @param nota         maturidade de 0 a 100 (clareza, metas mensuraveis, periodo, ausencia de sobreposicao)
 * @param sobreposicoes ids das estrategias vigentes que cobrem um objetivo parecido
 * @param fonte        GEMINI, OPENAI ou HEURISTICA
 */
public record AnaliseGerada(int nota, String resumo, List<String> pontosFortes, List<String> melhorias,
        List<String> sobreposicoes, String fonte) {
}
