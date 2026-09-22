package br.com.fiap.aguiainova.model;

import br.com.fiap.aguiainova.model.enums.StatusEstrategia;
import br.com.fiap.aguiainova.util.Textos;
import br.com.fiap.aguiainova.util.Validacoes;

/**
 * Conteudo editavel de uma estrategia, ja normalizado.
 *
 * @param status opcional: na criacao o padrao e VIGENTE; na atualizacao, se nulo, o status atual e mantido
 */
public record DadosEstrategia(String titulo, String descricao, String categoria, String campanha,
        StatusEstrategia status) {

    public DadosEstrategia {
        titulo = Validacoes.exigirTexto(titulo, "título");
        descricao = Validacoes.exigirTexto(descricao, "descrição");
        categoria = Validacoes.exigirTexto(categoria, "categoria");
        campanha = Textos.ouNulo(campanha);
    }
}
