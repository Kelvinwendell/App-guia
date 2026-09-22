package br.com.fiap.aguiainova.integration.ai;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Esqueleto comum dos provedores de IA que falam HTTP+JSON (Template Method): o fluxo de "montar pedido, enviar,
 * traduzir falhas, extrair texto" fica aqui; cada provedor so informa o que e especifico dele.
 *
 * @param <R> tipo da resposta JSON do provedor
 */
public abstract class ClienteLlmHttp<R> implements LlmClient {

    protected static final double TEMPERATURA = 0.3;

    private final RestClient http;
    private final String modelo;
    private final Class<R> tipoResposta;

    protected ClienteLlmHttp(RestClient http, String modelo, Class<R> tipoResposta) {
        this.http = http;
        this.modelo = modelo;
        this.tipoResposta = tipoResposta;
    }

    @Override
    public final String modelo() {
        return modelo;
    }

    @Override
    public final boolean disponivel() {
        return true;
    }

    @Override
    public final String gerar(PedidoLlm pedido) {
        try {
            R resposta = http.post()
                    .uri(caminho())
                    .headers(this::autenticar)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(montarCorpo(pedido))
                    .retrieve()
                    .body(tipoResposta);
            return extrairTexto(resposta);
        } catch (RestClientResponseException e) {
            throw new LlmIndisponivelException(
                    "O provedor " + provedor() + " respondeu com HTTP " + e.getStatusCode().value() + ".", e);
        } catch (RestClientException e) {
            throw new LlmIndisponivelException(
                    "Não foi possível contatar o provedor " + provedor() + ": " + e.getClass().getSimpleName(), e);
        }
    }

    /** Caminho do endpoint, relativo a URL base do cliente. */
    protected abstract String caminho();

    /** Acrescenta as credenciais ao pedido. A chave vai sempre em cabecalho, nunca na URL (que aparece em logs). */
    protected abstract void autenticar(HttpHeaders cabecalhos);

    protected abstract Map<String, Object> montarCorpo(PedidoLlm pedido);

    /** @throws LlmIndisponivelException se a resposta nao trouxer texto utilizavel */
    protected abstract String extrairTexto(R resposta);
}
