package br.com.fiap.aguiainova.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Numeros escritos em textos para pessoas (motivos, insights por regras): sempre no formato brasileiro. */
public final class Numeros {

    private static final Locale PT_BR = Locale.of("pt", "BR");

    private Numeros() {
    }

    /**
     * "13,08%", "-40%", "1.250,5%" ou "n/d". Antes os textos usavam {@code toPlainString()} e saiam com ponto
     * ("13.08%"), diferente do app, que mostra virgula.
     */
    public static String percentual(BigDecimal valor) {
        if (valor == null) {
            return "n/d";
        }
        // DecimalFormat nao e thread-safe: um por chamada (e barato perto do resto da geracao de texto).
        DecimalFormat formato = new DecimalFormat("#,##0.##", DecimalFormatSymbols.getInstance(PT_BR));
        return formato.format(valor) + "%";
    }
}
