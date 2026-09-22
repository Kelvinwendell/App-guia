package br.com.fiap.aguiainova.integration;

import java.util.List;

import br.com.fiap.aguiainova.integration.ai.GeminiLlmClient;
import br.com.fiap.aguiainova.integration.ai.LlmIndisponivelException;
import br.com.fiap.aguiainova.integration.ai.MensagemLlm;
import br.com.fiap.aguiainova.integration.ai.PedidoLlm;
import br.com.fiap.aguiainova.model.enums.PapelMensagem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GeminiLlmClientTest {

    private static final String CHAVE = "CHAVE-SECRETA-123";
    private static final String URL = "https://gemini.teste/v1beta/models/gemini-x:generateContent";

    private MockRestServiceServer servidor;
    private GeminiLlmClient cliente;

    @BeforeEach
    void preparar() {
        RestClient.Builder construtor = RestClient.builder().baseUrl("https://gemini.teste/v1beta");
        servidor = MockRestServiceServer.bindTo(construtor).build();
        cliente = new GeminiLlmClient(construtor.build(), "gemini-x", CHAVE);
    }

    private static PedidoLlm pedido(boolean json) {
        return new PedidoLlm("Você é um analista", List.of(
                new MensagemLlm(PapelMensagem.USUARIO, "Pergunta"),
                new MensagemLlm(PapelMensagem.ASSISTENTE, "Resposta anterior"),
                new MensagemLlm(PapelMensagem.USUARIO, "Nova pergunta")), json);
    }

    @Test
    void enviaOPedidoNoFormatoDoGeminiComAChaveNoCabecalho() {
        servidor.expect(requestTo(URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-goog-api-key", CHAVE))
                .andExpect(jsonPath("$.systemInstruction.parts[0].text").value("Você é um analista"))
                .andExpect(jsonPath("$.contents.length()").value(3))
                .andExpect(jsonPath("$.contents[0].role").value("user"))
                .andExpect(jsonPath("$.contents[0].parts[0].text").value("Pergunta"))
                .andExpect(jsonPath("$.contents[1].role").value("model"))
                .andExpect(jsonPath("$.contents[2].role").value("user"))
                .andExpect(jsonPath("$.generationConfig.responseMimeType").value("application/json"))
                .andExpect(jsonPath("$.generationConfig.temperature").value(0.3))
                .andRespond(withSuccess("{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"{\\\"ok\\\":true}\"}]}}]}",
                        MediaType.APPLICATION_JSON));

        assertThat(cliente.gerar(pedido(true))).isEqualTo("{\"ok\":true}");
        servidor.verify();
    }

    @Test
    void naoPedeJsonQuandoOPedidoNaoPrecisa() {
        servidor.expect(requestTo(URL))
                .andExpect(jsonPath("$.generationConfig.responseMimeType").doesNotExist())
                .andRespond(withSuccess("{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"olá\"}]}}]}",
                        MediaType.APPLICATION_JSON));

        assertThat(cliente.gerar(pedido(false))).isEqualTo("olá");
    }

    @Test
    void juntaTodasAsPartesDaResposta() {
        servidor.expect(requestTo(URL)).andRespond(withSuccess(
                "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"olá\"},{\"text\":\" mundo\"}]}}],"
                        + "\"usageMetadata\":{\"totalTokenCount\":9}}", MediaType.APPLICATION_JSON));

        assertThat(cliente.gerar(pedido(false))).isEqualTo("olá mundo");
    }

    @Test
    void falhaHttpViraIndisponibilidadeSemVazarAChave() {
        servidor.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
                .body("{\"error\":{\"message\":\"quota\"}}").contentType(MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> cliente.gerar(pedido(false)))
                .isInstanceOf(LlmIndisponivelException.class)
                .hasMessageContaining("HTTP 429")
                .hasMessageNotContaining(CHAVE);
    }

    @Test
    void respostaSemCandidatosOuVaziaViraIndisponibilidade() {
        servidor.expect(requestTo(URL)).andRespond(withSuccess("{\"candidates\":[]}", MediaType.APPLICATION_JSON));
        assertThatThrownBy(() -> cliente.gerar(pedido(false))).isInstanceOf(LlmIndisponivelException.class)
                .hasMessageContaining("bloqueio");

        servidor.reset();
        servidor.expect(requestTo(URL)).andRespond(withSuccess(
                "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"  \"}]}}]}", MediaType.APPLICATION_JSON));
        assertThatThrownBy(() -> cliente.gerar(pedido(false))).isInstanceOf(LlmIndisponivelException.class)
                .hasMessageContaining("vazia");
    }

    @Test
    void identificaOProvedorEOModelo() {
        assertThat(cliente.provedor()).isEqualTo("GEMINI");
        assertThat(cliente.modelo()).isEqualTo("gemini-x");
        assertThat(cliente.disponivel()).isTrue();
    }
}
