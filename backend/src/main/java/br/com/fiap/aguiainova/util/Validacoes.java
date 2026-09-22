package br.com.fiap.aguiainova.util;

import br.com.fiap.aguiainova.exception.RegraDeNegocioException;

/** Guardas usadas pelo dominio para garantir que um objeto nunca exista em estado invalido. */
public final class Validacoes {

    private Validacoes() {
    }

    /** Devolve o texto sem espacos nas pontas; falha se estiver nulo ou em branco. */
    public static String exigirTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw obrigatorio(campo);
        }
        return valor.strip();
    }

    public static <T> T exigir(T valor, String campo) {
        if (valor == null) {
            throw obrigatorio(campo);
        }
        return valor;
    }

    private static RegraDeNegocioException obrigatorio(String campo) {
        return new RegraDeNegocioException("O campo " + campo + " é obrigatório.");
    }
}
