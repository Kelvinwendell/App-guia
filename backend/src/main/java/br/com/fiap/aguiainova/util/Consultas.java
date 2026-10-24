package br.com.fiap.aguiainova.util;

import java.util.regex.Pattern;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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

    /** Adiciona "campo = valor" a consulta somente se valor nao for nulo (filtro opcional por enum/objeto). */
    public static void seIgual(Query consulta, String campo, Object valor) {
        if (valor != null) {
            consulta.addCriteria(Criteria.where(campo).is(valor));
        }
    }

    /** Adiciona "campo = valor" a consulta somente se o texto estiver informado (filtro opcional por String). */
    public static void seInformado(Query consulta, String campo, String valor) {
        if (informado(valor)) {
            consulta.addCriteria(Criteria.where(campo).is(valor));
        }
    }

    /** Adiciona igualdade sem diferenciar maiusculas/minusculas somente se o texto estiver informado. */
    public static void seInformadoIgnorandoCaixa(Query consulta, String campo, String valor) {
        if (informado(valor)) {
            consulta.addCriteria(igualIgnorandoCaixa(campo, valor));
        }
    }
}
