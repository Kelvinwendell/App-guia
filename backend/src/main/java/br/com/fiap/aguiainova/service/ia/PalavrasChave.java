package br.com.fiap.aguiainova.service.ia;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import br.com.fiap.aguiainova.util.Textos;
import org.springframework.util.StringUtils;

/**
 * Comparacao de textos por palavras, sem rede e sem IA: base das regras de contingencia que medem o quanto uma ideia
 * se parece com uma estrategia (ou uma estrategia com outra).
 */
final class PalavrasChave {

    private static final int TAMANHO_MINIMO_PALAVRA = 4;
    private static final int TAMANHO_RAIZ = 5;
    private static final Set<String> PALAVRAS_VAZIAS = Set.of("para", "como", "mais", "isso", "esta", "esse", "essa",
            "pelo", "pela", "seus", "suas", "entre", "sobre", "quando", "onde", "muito", "pode", "podem", "fazer",
            "cada", "todo", "toda", "todos", "todas", "ainda", "assim", "apenas", "tambem", "porque", "nossa",
            "nosso", "deve", "devem", "ideia", "empresa");

    private PalavrasChave() {
    }

    /** Palavras normalizadas (minusculas, sem acento, sem palavras vazias, reduzidas a uma raiz curta). */
    static Set<String> de(String... textos) {
        String junto = Arrays.stream(textos).filter(StringUtils::hasText).collect(Collectors.joining(" "));
        return Arrays.stream(Textos.semAcentos(junto).toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
                .filter(palavra -> palavra.length() >= TAMANHO_MINIMO_PALAVRA)
                .filter(palavra -> !PALAVRAS_VAZIAS.contains(palavra))
                .map(palavra -> palavra.substring(0, Math.min(palavra.length(), TAMANHO_RAIZ)))
                .collect(Collectors.toCollection(HashSet::new));
    }

    /** Fracao das palavras do menor dos dois conjuntos que aparecem no outro (0 a 1). */
    static double afinidade(Set<String> a, Set<String> b) {
        if (a.isEmpty() || b.isEmpty()) {
            return 0;
        }
        long emComum = a.stream().filter(b::contains).count();
        return (double) emComum / Math.min(a.size(), b.size());
    }
}
