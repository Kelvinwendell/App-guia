package br.com.fiap.aguiainova.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public final class Textos {

    private static final Pattern MARCAS_DE_ACENTO = Pattern.compile("\\p{M}+");
    private static final Pattern ESPACOS = Pattern.compile("\\s+");
    private static final String RETICENCIAS = "…";

    private Textos() {
    }

    /** Texto sem espacos nas pontas, ou nulo quando estiver nulo ou em branco (normaliza campos opcionais). */
    public static String ouNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.strip();
    }

    public static String semAcentos(String texto) {
        return MARCAS_DE_ACENTO.matcher(Normalizer.normalize(texto, Normalizer.Form.NFD)).replaceAll("");
    }

    /** Corta o texto em {@code maximo} caracteres (contando as reticencias), sem falhar para nulo. */
    public static String truncar(String texto, int maximo) {
        if (texto == null) {
            return "";
        }
        String limpo = texto.strip();
        if (limpo.length() <= maximo) {
            return limpo;
        }
        return limpo.substring(0, maximo - RETICENCIAS.length()).stripTrailing() + RETICENCIAS;
    }

    /**
     * Prepara um texto de terceiros para ser embutido em um prompt: numa linha so e sem caracteres que
     * permitiriam fechar as tags de dados ({@code <ideia>}, {@code <contexto>}...) e injetar instrucoes.
     */
    public static String paraPrompt(String texto, int maximo) {
        String umaLinha = ESPACOS.matcher(texto == null ? "" : texto).replaceAll(" ");
        String seguro = umaLinha.replace('<', '(').replace('>', ')').replace('"', '\'');
        return truncar(seguro, maximo);
    }
}
