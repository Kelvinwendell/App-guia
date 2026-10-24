package br.com.fiap.aguiainova.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import br.com.fiap.aguiainova.model.enums.StatusEstrategia;

public record EstrategiaResponse(
        String id,
        String titulo,
        String descricao,
        String categoria,
        String campanha,
        StatusEstrategia status,
        List<String> metas,
        /** Nulo quando o período não foi definido. */
        LocalDate inicio,
        LocalDate fim,
        String autorId,
        String autorNome,
        Instant criadoEm,
        Instant atualizadoEm) {
}
