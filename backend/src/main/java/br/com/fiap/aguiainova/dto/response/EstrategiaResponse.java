package br.com.fiap.aguiainova.dto.response;

import java.time.Instant;

import br.com.fiap.aguiainova.model.enums.StatusEstrategia;

public record EstrategiaResponse(
        String id,
        String titulo,
        String descricao,
        String categoria,
        String campanha,
        StatusEstrategia status,
        String autorId,
        String autorNome,
        Instant criadoEm,
        Instant atualizadoEm) {
}
