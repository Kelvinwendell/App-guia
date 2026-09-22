package br.com.fiap.aguiainova.model;

import java.time.Instant;
import java.util.List;

import br.com.fiap.aguiainova.model.enums.Prioridade;

/**
 * Pontuacao sugerida pela IA (ou pela heuristica de contingencia) para uma ideia. E apenas uma sugestao:
 * quem aprova, rejeita e define a prioridade final e sempre o gestor.
 *
 * @param fonte GEMINI, OPENAI (API compativel) ou HEURISTICA (regras locais, usada sem IA disponivel)
 */
public record AvaliacaoIa(
        int score,
        int alinhamentoEstrategico,
        Prioridade prioridadeSugerida,
        String justificativa,
        List<String> pontosFortes,
        List<String> riscos,
        String fonte,
        Instant avaliadaEm) {
}
