package br.com.fiap.aguiainova.config;

import java.time.Duration;
import java.util.Locale;

import br.com.fiap.aguiainova.integration.ai.GeminiLlmClient;
import br.com.fiap.aguiainova.integration.ai.LlmClient;
import br.com.fiap.aguiainova.integration.ai.LlmDesabilitadoClient;
import br.com.fiap.aguiainova.integration.ai.OpenAiCompatibleLlmClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

/** Escolhe o provedor de IA pela configuracao {@code app.ai.*}. */
@Configuration
public class IaConfig {

    private static final String PROVEDOR_GEMINI = "gemini";
    private static final String PROVEDOR_OPENAI = "openai";
    private static final String URL_PADRAO_GEMINI = "https://generativelanguage.googleapis.com/v1beta";
    private static final String URL_PADRAO_OPENAI = "https://openrouter.ai/api/v1";

    @Bean
    LlmClient llmClient(AppProperties props) {
        AppProperties.Ai ai = props.ai();
        String provedor = ai.provider().trim().toLowerCase(Locale.ROOT);
        if (!provedor.equals(PROVEDOR_GEMINI) && !provedor.equals(PROVEDOR_OPENAI)) {
            throw new IllegalStateException(
                    "app.ai.provider inválido: '" + ai.provider() + "'. Use '" + PROVEDOR_GEMINI + "' ou '"
                            + PROVEDOR_OPENAI + "'.");
        }
        if (!ai.configurada()) {
            return new LlmDesabilitadoClient();
        }
        if (provedor.equals(PROVEDOR_GEMINI)) {
            return new GeminiLlmClient(clienteHttp(ai, URL_PADRAO_GEMINI), ai.model(), ai.apiKey());
        }
        return new OpenAiCompatibleLlmClient(clienteHttp(ai, URL_PADRAO_OPENAI), ai.model(), ai.apiKey());
    }

    private static RestClient clienteHttp(AppProperties.Ai ai, String urlPadrao) {
        Duration limite = Duration.ofSeconds(ai.timeoutSeconds());
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(limite);
        fabrica.setReadTimeout(limite);
        return RestClient.builder().baseUrl(urlBase(ai, urlPadrao)).requestFactory(fabrica).build();
    }

    private static String urlBase(AppProperties.Ai ai, String urlPadrao) {
        String url = StringUtils.hasText(ai.baseUrl()) ? ai.baseUrl().trim() : urlPadrao;
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
