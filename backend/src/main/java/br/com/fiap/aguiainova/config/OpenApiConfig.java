package br.com.fiap.aguiainova.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA = "bearerAuth";

    @Bean
    OpenAPI aguiaInovaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Águia Inova API")
                        .version("1.0")
                        .description("""
                                API REST da plataforma Águia Inova (gestão da inovação empresarial).
                                Faça login em **POST /api/auth/login**, copie o `accessToken` e clique em \
                                **Authorize** para testar os demais endpoints.
                                Perfis: OPERADOR, GESTOR e LIDERANCA."""))
                .components(new Components().addSecuritySchemes(ESQUEMA, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA));
    }
}
