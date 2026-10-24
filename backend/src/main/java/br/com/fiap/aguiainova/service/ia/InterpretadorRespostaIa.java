package br.com.fiap.aguiainova.service.ia;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
    private static final String RESUMO_PADRAO = "Sem resumo informado pelo modelo.";
    private static final String CATEGORIA_PADRAO = "Geral";
    // Limites iguais aos da validacao de EstrategiaRequest: o rascunho tem de poder ser salvo sem ajustes de tamanho.
    private static final int MAXIMO_TITULO = 150;
    private static final int MAXIMO_DESCRICAO_ESTRATEGIA = 2000;
    private static final int MAXIMO_CATEGORIA = 80;
    private static final int MAXIMO_CAMPANHA = 120;
    private static final int MAXIMO_META = 200;

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

    public RascunhoGerado interpretarRascunho(String texto, String fonte) {
        RespostaRascunho resposta = ler(texto, RespostaRascunho.class);
        String titulo = Textos.truncar(resposta.titulo(), MAXIMO_TITULO);
        String descricao = Textos.truncar(resposta.descricao(), MAXIMO_DESCRICAO_ESTRATEGIA);
        if (titulo.isBlank() || descricao.isBlank()) {
            throw new LlmIndisponivelException("A IA não devolveu um rascunho utilizável.");
        }
        String categoria = Textos.truncar(resposta.categoria(), MAXIMO_CATEGORIA);
        LocalDate inicio = lerData(resposta.inicio());
        LocalDate fim = lerData(resposta.fim());
        if (inicio != null && fim != null && fim.isBefore(inicio)) {
            inicio = null;
            fim = null;
        }
        return new RascunhoGerado(titulo, descricao, categoria.isBlank() ? CATEGORIA_PADRAO : categoria,
                Textos.ouNulo(Textos.truncar(resposta.campanha(), MAXIMO_CAMPANHA)),
                limparLista(resposta.metas(), MAXIMO_META), inicio, fim, fonte);
    }

    /** As sobreposicoes citadas pelo modelo so valem se forem ids de estrategias que ele realmente recebeu. */
    public AnaliseGerada interpretarAnalise(String texto, String fonte, Set<String> idsConhecidos) {
        RespostaAnalise resposta = ler(texto, RespostaAnalise.class);
        if (resposta.nota() == null) {
            throw new LlmIndisponivelException("A IA não devolveu uma análise utilizável.");
        }
        String resumo = Textos.truncar(resposta.resumo(), MAXIMO_JUSTIFICATIVA);
        List<String> sobreposicoes = resposta.sobreposicoes() == null ? List.of() : resposta.sobreposicoes().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(idsConhecidos::contains)
                .distinct()
                .toList();
        return new AnaliseGerada(limitar(resposta.nota()), resumo.isBlank() ? RESUMO_PADRAO : resumo,
                limparLista(resposta.pontosFortes()), limparLista(resposta.melhorias()), sobreposicoes, fonte);
    }

    public LeituraGerada interpretarLeitura(String texto, String fonte) {
        RespostaLeitura resposta = ler(texto, RespostaLeitura.class);
        String leitura = Textos.truncar(resposta.leitura(), MAXIMO_JUSTIFICATIVA);
        if (leitura.isBlank()) {
            throw new LlmIndisponivelException("A IA não devolveu uma leitura utilizável.");
        }
        return new LeituraGerada(leitura, limparLista(resposta.recomendacoes()), fonte);
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
        return limparLista(itens, MAXIMO_TEXTO_ITEM);
    }

    private static List<String> limparLista(List<String> itens, int maximoPorItem) {
        if (itens == null) {
            return List.of();
        }
        return itens.stream()
                .filter(Objects::nonNull)
                .filter(StringUtils::hasText)
                .limit(MAXIMO_ITENS_LISTA)
                .map(item -> Textos.truncar(item, maximoPorItem))
                .toList();
    }

    /** Data no formato ISO (yyyy-MM-dd) ou nulo se o modelo devolveu algo que nao e uma data. */
    private static LocalDate lerData(String valor) {
        if (!StringUtils.hasText(valor)) {
            return null;
        }
        try {
            return LocalDate.parse(valor.strip());
        } catch (DateTimeParseException e) {
            return null;
        }
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RespostaRascunho(String titulo, String descricao, String categoria, String campanha, List<String> metas,
            String inicio, String fim) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RespostaAnalise(Integer nota, String resumo, List<String> pontosFortes, List<String> melhorias,
            List<String> sobreposicoes) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RespostaLeitura(String leitura, List<String> recomendacoes) {
    }
}
