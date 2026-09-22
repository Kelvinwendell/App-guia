package br.com.fiap.aguiainova.dto.response;

import java.time.Instant;

import br.com.fiap.aguiainova.model.enums.AcaoEstrategia;
import br.com.fiap.aguiainova.model.enums.StatusEstrategia;

public record EstrategiaHistoricoResponse(
        String id,
        String estrategiaId,
        Instant data,
        String categoria,
        String campanha,
        AcaoEstrategia acao,
        String titulo,
        String descricao,
        StatusEstrategia status,
        String autorNome) {
}
