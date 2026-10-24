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
        @Valid @NotNull Ai ai,
        @Valid @NotNull RateLimit rateLimit,
        @Valid @NotNull Dashboard dashboard) {

    public record Jwt(@NotBlank String secret, @Min(1) long expirationMinutes, @NotBlank String issuer) {
    }

    public record Security(boolean allowRoleSelection) {
    }

    public record Seed(boolean enabled, @Valid @NotNull Demo demo) {

        /**
         * Dados de demonstracao para apresentacoes: {@code modo} carrega ("carregar" ou "true") ou remove ("remover")
         * um conjunto fixo de contas, estrategias, ideias e projetos ficticios; qualquer outro valor deixa tudo
         * desligado. A senha das contas de demonstracao nunca fica no repositorio: vem do ambiente.
         */
        public record Demo(String modo, String senha) {

            public boolean carregar() {
                return modoEm("carregar", "true", "sim", "1");
            }

            public boolean remover() {
                return modoEm("remover", "limpar");
            }

            private boolean modoEm(String... aceitos) {
                if (modo == null) {
                    return false;
                }
                String normalizado = modo.strip();
                for (String aceito : aceitos) {
                    if (aceito.equalsIgnoreCase(normalizado)) {
                        return true;
                    }
                }
                return false;
            }
        }
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

    /** Limites de requisições por minuto, por chave (e-mail no login, id de usuário na IA). */
    public record RateLimit(@Min(1) int loginPorMinuto, @Min(1) int iaPorMinuto) {
    }

    /** Teto de documentos lidos por agregação do dashboard, para não carregar a coleção inteira na memória. */
    public record Dashboard(@Min(1) int limiteRegistrosAgregacao) {
    }
}
