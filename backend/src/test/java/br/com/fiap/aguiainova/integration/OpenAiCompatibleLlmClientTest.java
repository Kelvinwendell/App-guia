package br.com.fiap.aguiainova.integration;

import java.util.List;

import br.com.fiap.aguiainova.integration.ai.LlmIndisponivelException;
import br.com.fiap.aguiainova.integration.ai.MensagemLlm;
import br.com.fiap.aguiainova.integration.ai.OpenAiCompatibleLlmClient;
import br.com.fiap.aguiainova.integration.ai.PedidoLlm;
import br.com.fiap.aguiainova.model.enums.PapelMensagem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenAiCompatibleLlmClientTest {

    private static final String CHAVE = "sk-secreta";
    private static final String URL = "https://openrouter.teste/api/v1/chat/completions";

    private MockRestServiceServer servidor;
    private OpenAiCompatibleLlmClient cliente;

    @BeforeEach
    void preparar() {
        RestClient.Builder construtor = RestClient.builder().baseUrl("https://openrouter.teste/api/v1");
        servidor = MockRestServiceServer.bindTo(construtor).build();
        cliente = new OpenAiCompatibleLlmClient(construtor.build(), "meta/modelo-gratis", CHAVE);
    }

    private static PedidoLlm pedido() {
        return new PedidoLlm("Regras do sistema", List.of(
                new MensagemLlm(PapelMensagem.USUARIO, "Pergunta"),
                new MensagemLlm(PapelMensagem.ASSISTENTE, "Resposta"),
                new MensagemLlm(PapelMensagem.USUARIO, "Outra pergunta")), false);
    }

    @Test
    void enviaMensagensNoFormatoChatCompletionsComBearer() {
        servidor.expect(requestTo(URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer " + CHAVE))
                .andExpect(jsonPath("$.model").value("meta/modelo-gratis"))
                .andExpect(jsonPath("$.messages.length()").value(4))
                .andExpect(jsonPath("$.messages[0].role").value("system"))
                .andExpect(jsonPath("$.messages[0].content").value("Regras do sistema"))
                .andExpect(jsonPath("$.messages[1].role").value("user"))
                .andExpect(jsonPath("$.messages[2].role").value("assistant"))
                .andExpect(jsonPath("$.messages[3].content").value("Outra pergunta"))
                .andRespond(withSuccess("{\"id\":\"1\",\"choices\":[{\"index\":0,\"message\":"
                        + "{\"role\":\"assistant\",\"content\":\"Olá!\"}}]}", MediaType.APPLICATION_JSON));

        assertThat(cliente.gerar(pedido())).isEqualTo("Olá!");
        servidor.verify();
    }

    @Test
    void erroDoServidorViraIndisponibilidadeSemVazarAChave() {
        servidor.expect(requestTo(URL)).andRespond(withServerError());

        assertThatThrownBy(() -> cliente.gerar(pedido()))
                .isInstanceOf(LlmIndisponivelException.class)
                .hasMessageContaining("HTTP 500")
                .hasMessageNotContaining(CHAVE);
    }

    @Test
    void semEscolhasOuComConteudoVazioViraIndisponibilidade() {
        servidor.expect(requestTo(URL)).andRespond(withSuccess("{\"choices\":[]}", MediaType.APPLICATION_JSON));
        assertThatThrownBy(() -> cliente.gerar(pedido())).isInstanceOf(LlmIndisponivelException.class);

        servidor.reset();
        servidor.expect(requestTo(URL)).andRespond(withSuccess(
                "{\"choices\":[{\"message\":{\"content\":\"\"}}]}", MediaType.APPLICATION_JSON));
        assertThatThrownBy(() -> cliente.gerar(pedido())).isInstanceOf(LlmIndisponivelException.class);
    }

    @Test
    void identificaOProvedorEOModelo() {
        assertThat(cliente.provedor()).isEqualTo("OPENAI");
        assertThat(cliente.modelo()).isEqualTo("meta/modelo-gratis");
    }
}
