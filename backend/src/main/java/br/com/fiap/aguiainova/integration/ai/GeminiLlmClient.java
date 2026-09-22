package br.com.fiap.aguiainova.integration.ai;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import br.com.fiap.aguiainova.model.enums.PapelMensagem;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

/** Adaptador da API REST do Google Gemini (metodo {@code generateContent}). */
public class GeminiLlmClient extends ClienteLlmHttp<GeminiLlmClient.RespostaGemini> {

    private static final String CABECALHO_CHAVE = "x-goog-api-key";

    private final String chaveApi;

    public GeminiLlmClient(RestClient http, String modelo, String chaveApi) {
        super(http, modelo, RespostaGemini.class);
        this.chaveApi = chaveApi;
    }

    @Override
    public String provedor() {
        return "GEMINI";
    }

    @Override
    protected String caminho() {
        return "/models/" + modelo() + ":generateContent";
    }

    @Override
    protected void autenticar(HttpHeaders cabecalhos) {
        cabecalhos.set(CABECALHO_CHAVE, chaveApi);
    }

    @Override
    protected Map<String, Object> montarCorpo(PedidoLlm pedido) {
        Map<String, Object> configuracao = new LinkedHashMap<>();
        configuracao.put("temperature", TEMPERATURA);
        if (pedido.respostaJson()) {
            configuracao.put("responseMimeType", "application/json");
        }
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("systemInstruction", Map.of("parts", List.of(Map.of("text", pedido.instrucaoSistema()))));
        corpo.put("contents", pedido.mensagens().stream().map(GeminiLlmClient::conteudo).toList());
        corpo.put("generationConfig", configuracao);
        return corpo;
    }

    @Override
    protected String extrairTexto(RespostaGemini resposta) {
        if (resposta == null || resposta.candidates() == null || resposta.candidates().isEmpty()) {
            throw new LlmIndisponivelException("O Gemini não devolveu candidatos (possível bloqueio de segurança).");
        }
        Conteudo conteudo = resposta.candidates().get(0).content();
        String texto = conteudo == null || conteudo.parts() == null ? "" : conteudo.parts().stream()
                .map(Parte::text)
                .filter(Objects::nonNull)
                .collect(Collectors.joining());
        if (texto.isBlank()) {
            throw new LlmIndisponivelException("O Gemini devolveu uma resposta vazia.");
        }
        return texto;
    }

    private static Map<String, Object> conteudo(MensagemLlm mensagem) {
        String papel = mensagem.papel() == PapelMensagem.ASSISTENTE ? "model" : "user";
        return Map.of("role", papel, "parts", List.of(Map.of("text", mensagem.texto())));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RespostaGemini(List<Candidato> candidates) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Candidato(Conteudo content) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Conteudo(List<Parte> parts) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Parte(String text) {
    }
}
