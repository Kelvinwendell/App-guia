package br.com.fiap.aguiainova.integration.ai;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.fiap.aguiainova.model.enums.PapelMensagem;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

/**
 * Adaptador para qualquer API compativel com o formato "chat completions" da OpenAI, como OpenRouter
 * (modelos gratuitos) e GitHub Models.
 */
public class OpenAiCompatibleLlmClient extends ClienteLlmHttp<OpenAiCompatibleLlmClient.RespostaChat> {

    private final String chaveApi;

    public OpenAiCompatibleLlmClient(RestClient http, String modelo, String chaveApi) {
        super(http, modelo, RespostaChat.class);
        this.chaveApi = chaveApi;
    }

    @Override
    public String provedor() {
        return "OPENAI";
    }

    @Override
    protected String caminho() {
        return "/chat/completions";
    }

    @Override
    protected void autenticar(HttpHeaders cabecalhos) {
        cabecalhos.setBearerAuth(chaveApi);
    }

    @Override
    protected Map<String, Object> montarCorpo(PedidoLlm pedido) {
        List<Map<String, String>> mensagens = new ArrayList<>();
        mensagens.add(Map.of("role", "system", "content", pedido.instrucaoSistema()));
        pedido.mensagens().forEach(mensagem -> mensagens.add(Map.of(
                "role", mensagem.papel() == PapelMensagem.ASSISTENTE ? "assistant" : "user",
                "content", mensagem.texto())));
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("model", modelo());
        corpo.put("temperature", TEMPERATURA);
        corpo.put("messages", mensagens);
        return corpo;
    }

    @Override
    protected String extrairTexto(RespostaChat resposta) {
        if (resposta == null || resposta.choices() == null || resposta.choices().isEmpty()
                || resposta.choices().get(0).message() == null) {
            throw new LlmIndisponivelException("O provedor de IA não devolveu nenhuma escolha.");
        }
        String texto = resposta.choices().get(0).message().content();
        if (texto == null || texto.isBlank()) {
            throw new LlmIndisponivelException("O provedor de IA devolveu uma resposta vazia.");
        }
        return texto;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RespostaChat(List<Escolha> choices) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Escolha(Mensagem message) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Mensagem(String content) {
    }
}
