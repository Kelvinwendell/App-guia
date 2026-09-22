package br.com.fiap.aguiainova.service.ia;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import br.com.fiap.aguiainova.model.AvaliacaoIa;
import br.com.fiap.aguiainova.model.Estrategia;
import br.com.fiap.aguiainova.model.Ideia;
import br.com.fiap.aguiainova.model.enums.Prioridade;
import br.com.fiap.aguiainova.util.Textos;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Pontuacao por regras, deterministica e sem rede: plano de contingencia para quando a IA nao esta disponivel.
 * A nota soma "completude" da proposta (ate 40 pontos) e "alinhamento" com as estrategias vigentes (ate 60),
 * medido pela sobreposicao de palavras e pela categoria. O resultado e marcado com a fonte HEURISTICA.
 */
@Component
public class AvaliadorHeuristico implements AvaliadorDeIdeias {

    private static final int PONTOS_DESCRICAO = 25;
    private static final int CARACTERES_DESCRICAO_COMPLETA = 300;
    private static final int CARACTERES_DESCRICAO_CURTA = 80;
    private static final int PONTOS_TITULO = 5;
    private static final int CARACTERES_TITULO_DESCRITIVO = 15;
    private static final int PONTOS_VINCULO = 10;
    private static final int PONTOS_ALINHAMENTO = 60;
    private static final int PERCENTUAL_ALINHAMENTO_BOM = 50;
    private static final int PERCENTUAL_ALINHAMENTO_BAIXO = 20;
    private static final double BONUS_MESMA_CATEGORIA = 0.25;
    private static final int TAMANHO_MINIMO_PALAVRA = 4;
    private static final int TAMANHO_RAIZ = 5;
    private static final Set<String> PALAVRAS_VAZIAS = Set.of("para", "como", "mais", "isso", "esta", "esse", "essa",
            "pelo", "pela", "seus", "suas", "entre", "sobre", "quando", "onde", "muito", "pode", "podem", "fazer",
            "cada", "todo", "toda", "todos", "todas", "ainda", "assim", "apenas", "tambem", "porque", "nossa",
            "nosso", "deve", "devem", "ideia", "empresa");

    private final Clock clock;

    public AvaliadorHeuristico(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Map<String, AvaliacaoIa> avaliar(List<Ideia> ideias, List<Estrategia> estrategiasVigentes) {
        Instant agora = clock.instant();
        Map<String, AvaliacaoIa> avaliacoes = new HashMap<>();
        for (Ideia ideia : ideias) {
            avaliacoes.put(ideia.getId(), avaliar(ideia, estrategiasVigentes, agora));
        }
        return avaliacoes;
    }

    private AvaliacaoIa avaliar(Ideia ideia, List<Estrategia> estrategias, Instant agora) {
        Alinhamento alinhamento = melhorAlinhamento(ideia, estrategias);
        int pontosAlinhamento = alinhamento.percentual() * PONTOS_ALINHAMENTO / 100;
        int score = Math.min(100, pontosDeCompletude(ideia) + pontosAlinhamento);
        return new AvaliacaoIa(score, alinhamento.percentual(), Prioridade.sugeridaParaScore(score),
                justificativa(ideia, alinhamento), pontosFortes(ideia, alinhamento),
                riscos(ideia, alinhamento, estrategias), FonteIa.HEURISTICA, agora);
    }

    private static int pontosDeCompletude(Ideia ideia) {
        int tamanhoDescricao = Math.min(ideia.getDescricao().length(), CARACTERES_DESCRICAO_COMPLETA);
        int pontos = tamanhoDescricao * PONTOS_DESCRICAO / CARACTERES_DESCRICAO_COMPLETA;
        if (ideia.getTitulo().length() >= CARACTERES_TITULO_DESCRITIVO) {
            pontos += PONTOS_TITULO;
        }
        if (StringUtils.hasText(ideia.getEstrategiaId())) {
            pontos += PONTOS_VINCULO;
        }
        return pontos;
    }

    private static Alinhamento melhorAlinhamento(Ideia ideia, List<Estrategia> estrategias) {
        Set<String> palavrasDaIdeia = palavras(ideia.getTitulo(), ideia.getDescricao(), ideia.getCategoria());
        Alinhamento melhor = new Alinhamento(0, null);
        for (Estrategia estrategia : estrategias) {
            double afinidade = afinidade(palavrasDaIdeia, estrategia);
            if (ideia.getCategoria().equalsIgnoreCase(estrategia.getCategoria())) {
                afinidade += BONUS_MESMA_CATEGORIA;
            }
            int percentual = (int) Math.round(Math.min(1.0, afinidade) * 100);
            if (percentual > melhor.percentual()) {
                melhor = new Alinhamento(percentual, estrategia.getTitulo());
            }
        }
        return melhor;
    }

    /** Fracao das palavras do menor dos dois textos que aparecem no outro (0 a 1). */
    private static double afinidade(Set<String> palavrasDaIdeia, Estrategia estrategia) {
        Set<String> palavrasDaEstrategia = palavras(estrategia.getTitulo(), estrategia.getDescricao(),
                estrategia.getCategoria(), estrategia.getCampanha());
        if (palavrasDaIdeia.isEmpty() || palavrasDaEstrategia.isEmpty()) {
            return 0;
        }
        long emComum = palavrasDaIdeia.stream().filter(palavrasDaEstrategia::contains).count();
        return (double) emComum / Math.min(palavrasDaIdeia.size(), palavrasDaEstrategia.size());
    }

    /** Palavras normalizadas (minusculas, sem acento, sem palavras vazias, reduzidas a uma raiz curta). */
    private static Set<String> palavras(String... textos) {
        String junto = Arrays.stream(textos).filter(StringUtils::hasText).collect(Collectors.joining(" "));
        return Arrays.stream(Textos.semAcentos(junto).toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
                .filter(palavra -> palavra.length() >= TAMANHO_MINIMO_PALAVRA)
                .filter(palavra -> !PALAVRAS_VAZIAS.contains(palavra))
                .map(palavra -> palavra.substring(0, Math.min(palavra.length(), TAMANHO_RAIZ)))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private static String justificativa(Ideia ideia, Alinhamento alinhamento) {
        String estrategia = alinhamento.estrategia() == null
                ? "nenhuma estratégia vigente"
                : "a estratégia \"" + alinhamento.estrategia() + "\"";
        return "Avaliação automática por regras (IA indisponível): descrição com %d caracteres e alinhamento de %d%% com %s."
                .formatted(ideia.getDescricao().length(), alinhamento.percentual(), estrategia);
    }

    private static List<String> pontosFortes(Ideia ideia, Alinhamento alinhamento) {
        List<String> pontos = new ArrayList<>();
        if (ideia.getDescricao().length() >= CARACTERES_DESCRICAO_COMPLETA) {
            pontos.add("Descrição detalhada");
        }
        if (alinhamento.percentual() >= PERCENTUAL_ALINHAMENTO_BOM) {
            pontos.add("Bom alinhamento com a estratégia \"" + alinhamento.estrategia() + "\"");
        }
        if (StringUtils.hasText(ideia.getEstrategiaId())) {
            pontos.add("Já vinculada a uma estratégia vigente");
        }
        return pontos;
    }

    private static List<String> riscos(Ideia ideia, Alinhamento alinhamento, List<Estrategia> estrategias) {
        List<String> riscos = new ArrayList<>();
        if (ideia.getDescricao().length() < CARACTERES_DESCRICAO_CURTA) {
            riscos.add("Descrição curta: detalhar problema, solução e benefício esperado");
        }
        if (estrategias.isEmpty()) {
            riscos.add("Não há estratégias vigentes para comparar");
        } else if (alinhamento.percentual() < PERCENTUAL_ALINHAMENTO_BAIXO) {
            riscos.add("Baixo alinhamento com as estratégias vigentes");
        }
        return riscos;
    }

    private record Alinhamento(int percentual, String estrategia) {
    }
}
