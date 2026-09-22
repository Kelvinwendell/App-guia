package br.com.fiap.aguiainova.model;

import br.com.fiap.aguiainova.util.Textos;
import br.com.fiap.aguiainova.util.Validacoes;

/** Conteudo editavel de uma ideia, ja normalizado. */
public record DadosIdeia(String titulo, String descricao, String categoria, String estrategiaId) {

    public DadosIdeia {
        titulo = Validacoes.exigirTexto(titulo, "título");
        descricao = Validacoes.exigirTexto(descricao, "descrição");
        categoria = Validacoes.exigirTexto(categoria, "categoria");
        estrategiaId = Textos.ouNulo(estrategiaId);
    }
}
