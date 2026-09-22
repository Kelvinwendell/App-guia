package br.com.fiap.aguiainova.dto.response;

import br.com.fiap.aguiainova.model.AvaliacaoIa;
import br.com.fiap.aguiainova.model.enums.Prioridade;
import br.com.fiap.aguiainova.model.enums.StatusIdeia;

public record AvaliacaoIaResponse(
        String ideiaId,
        String titulo,
        String autorNome,
        StatusIdeia status,
        Prioridade prioridadeAtual,
        AvaliacaoIa avaliacao) {
}
