package br.com.fiap.aguiainova.dto.response;

import java.time.Instant;

import br.com.fiap.aguiainova.model.AvaliacaoIa;
import br.com.fiap.aguiainova.model.enums.Prioridade;
import br.com.fiap.aguiainova.model.enums.StatusIdeia;

public record IdeiaResponse(
        String id,
        String titulo,
        String descricao,
        String categoria,
        StatusIdeia status,
        Prioridade prioridade,
        String autorId,
        String autorNome,
        String estrategiaId,
        String estrategiaTitulo,
        /** Comentário do gestor ao aprovar/rejeitar. */
        String parecer,
        String avaliadorNome,
        Instant avaliadaEm,
        /** Só é preenchida para GESTOR: a pontuação da IA é uma ferramenta de triagem, não um feedback ao autor. */
        AvaliacaoIa avaliacaoIa,
        Instant criadoEm,
        Instant atualizadoEm) {
}
