package br.com.fiap.aguiainova.dto.response;

import java.time.LocalDate;

import br.com.fiap.aguiainova.model.enums.StatusProjeto;

public record PrazoProjetoResponse(
        String projetoId,
        String nome,
        StatusProjeto status,
        int progresso,
        LocalDate prazo,
        long diasParaPrazo,
        boolean atrasado) {
}
