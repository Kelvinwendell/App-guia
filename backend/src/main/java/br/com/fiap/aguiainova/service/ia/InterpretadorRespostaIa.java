package br.com.fiap.aguiainova.service.ia;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import br.com.fiap.aguiainova.integration.ai.LlmIndisponivelException;
import br.com.fiap.aguiainova.model.AvaliacaoIa;
import br.com.fiap.aguiainova.model.enums.Prioridade;
import br.com.fiap.aguiainova.util.Textos;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Converte o texto devolvido pelo modelo em objetos da aplicacao. A saida de um LLM nunca e confiavel: o JSON e
 * extraido de dentro de qualquer texto ao redor, os numeros sao limitados ao intervalo valido, valores
 * desconhecidos caem em padroes seguros e textos sao truncados.
 */
@Component
public class InterpretadorRespostaIa {

    private static final int SCORE_MAXIMO = 100;
    private static final int MAXIMO_ITENS_LISTA = 5;
    private static final int MAXIMO_TEXTO_ITEM = 250;
    private static final int MAXIMO_JUSTIFICATIVA = 600;
    private static final String JUSTIFICATIVA_PADRAO = "Sem justificativa informada pelo modelo.";

    private final JsonMapper json;

    public InterpretadorRespostaIa(JsonMapper json) {
        this.json = json;
    }

    /** Devolve as avaliacoes validas indexadas pelo id da ideia; ideias ausentes na resposta ficam de fora. */
    public Map<String, AvaliacaoIa> interpretarAvaliacoes(String texto, String fonte, Instant agora) {
        RespostaAvaliacoes resposta = ler(texto, RespostaAvaliacoes.class);
        Map<String, AvaliacaoIa> porIdeia = new HashMap<>();
        if (resposta.avaliacoes() == null) {
            return porIdeia;
        }
        for (ItemAvaliacao item : resposta.avaliacoes()) {
            if (item != null && item.id() != null && item.score() != null) {
                porIdeia.put(item.id().trim(), converter(item, fonte, agora));
            }
        }
        return porIdeia;
    }

    public InsightsGerados interpretarInsights(String texto, String fonte) {
        RespostaInsights resposta = ler(texto, RespostaInsights.class);
        String resumo = Textos.truncar(resposta.resumo(), MAXIMO_JUSTIFICATIVA);
        List<String> insights = limparLista(resposta.insights());
        List<String> recomendacoes = limparLista(resposta.recomendacoes());
        if (resumo.isBlank() && insights.isEmpty() && recomendacoes.isEmpty()) {
            throw new LlmIndisponivelException("A IA não devolveu insights utilizáveis.");
        }
        return new InsightsGerados(resumo, insights, recomendacoes, fonte);
    }

    private <T> T ler(String texto, Class<T> tipo) {
        int inicio = texto.indexOf('{');
        int fim = texto.lastIndexOf('}');
        if (inicio < 0 || fim <= inicio) {
            throw new LlmIndisponivelException("A resposta da IA não contém JSON.");
        }
        try {
            return json.readValue(texto.substring(inicio, fim + 1), tipo);
        } catch (JacksonException e) {
            throw new LlmIndisponivelException("A resposta da IA está em formato inválido.", e);
        }
    }

    private static AvaliacaoIa converter(ItemAvaliacao item, String fonte, Instant agora) {
        int score = limitar(item.score());
        int alinhamento = item.alinhamentoEstrategico() == null ? score : limitar(item.alinhamentoEstrategico());
        String justificativa = StringUtils.hasText(item.justificativa())
                ? Textos.truncar(item.justificativa(), MAXIMO_JUSTIFICATIVA)
                : JUSTIFICATIVA_PADRAO;
        return new AvaliacaoIa(score, alinhamento, interpretarPrioridade(item.prioridade(), score), justificativa,
                limparLista(item.pontosFortes()), limparLista(item.riscos()), fonte, agora);
    }

    private static Prioridade interpretarPrioridade(String valor, int score) {
        if (valor != null) {
            try {
                return Prioridade.valueOf(Textos.semAcentos(valor).trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                // valor fora do combinado: usa a prioridade derivada da nota
            }
        }
        return Prioridade.sugeridaParaScore(score);
    }

    private static int limitar(int valor) {
        return Math.max(0, Math.min(SCORE_MAXIMO, valor));
    }

    private static List<String> limparLista(List<String> itens) {
        if (itens == null) {
            return List.of();
        }
        return itens.stream()
                .filter(Objects::nonNull)
                .filter(StringUtils::hasText)
                .limit(MAXIMO_ITENS_LISTA)
                .map(item -> Textos.truncar(item, MAXIMO_TEXTO_ITEM))
                .toList();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RespostaAvaliacoes(List<ItemAvaliacao> avaliacoes) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ItemAvaliacao(String id, Integer score, Integer alinhamentoEstrategico, String prioridade,
            String justificativa, List<String> pontosFortes, List<String> riscos) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RespostaInsights(String resumo, List<String> insights, List<String> recomendacoes) {
    }
}
