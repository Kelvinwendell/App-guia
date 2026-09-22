package br.com.fiap.aguiainova.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Configuracoes proprias da aplicacao (prefixo {@code app} no application.yml). */
@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @Valid @NotNull Jwt jwt,
        @Valid @NotNull Security security,
        @Valid @NotNull Seed seed,
        @Valid @NotNull Ai ai) {

    public record Jwt(@NotBlank String secret, @Min(1) long expirationMinutes, @NotBlank String issuer) {
    }

    public record Security(boolean allowRoleSelection) {
    }

    public record Seed(boolean enabled) {
    }

    public record Ai(
            boolean enabled,
            @NotBlank String provider,
            String apiKey,
            @NotBlank String model,
            String baseUrl,
            @Min(1) int timeoutSeconds) {

        /** A IA so e chamada de fato quando esta habilitada e existe uma chave de API. */
        public boolean configurada() {
            return enabled && apiKey != null && !apiKey.isBlank();
        }
    }
}
