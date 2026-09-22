package br.com.fiap.aguiainova.dto.response;

import java.time.Instant;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        Instant expiraEm,
        UsuarioResponse usuario) {
}
