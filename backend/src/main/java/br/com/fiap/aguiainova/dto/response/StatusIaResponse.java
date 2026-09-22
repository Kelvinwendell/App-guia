package br.com.fiap.aguiainova.dto.response;

public record StatusIaResponse(
        boolean habilitada,
        /** Verdadeiro quando há provedor configurado (com chave); senão só a avaliação por regras funciona. */
        boolean disponivel,
        String provedor,
        String modelo) {
}
