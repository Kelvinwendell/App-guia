package br.com.fiap.aguiainova.util;

import java.util.regex.Pattern;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.util.StringUtils;

/** Pequenos auxiliares para montar filtros opcionais nas consultas do MongoDB. */
public final class Consultas {

    private Consultas() {
    }

    /** Igualdade sem diferenciar maiusculas/minusculas; o texto e escapado, entao nao vira expressao regular. */
    public static Criteria igualIgnorandoCaixa(String campo, String valor) {
        return Criteria.where(campo)
                .regex(Pattern.compile("^" + Pattern.quote(valor.trim()) + "$", Pattern.CASE_INSENSITIVE));
    }

    public static boolean informado(String valor) {
        return StringUtils.hasText(valor);
    }
}
