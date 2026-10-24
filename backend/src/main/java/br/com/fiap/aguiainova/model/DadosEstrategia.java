package br.com.fiap.aguiainova.model;

import java.time.LocalDate;
import java.util.List;

import br.com.fiap.aguiainova.exception.RegraDeNegocioException;
import br.com.fiap.aguiainova.model.enums.StatusEstrategia;
import br.com.fiap.aguiainova.util.Textos;
import br.com.fiap.aguiainova.util.Validacoes;

/**
 * Conteudo editavel de uma estrategia, ja normalizado.
 *
 * @param status opcional: na criacao o padrao e VIGENTE; na atualizacao, se nulo, o status atual e mantido
 * @param metas  resultados esperados (de preferencia mensuraveis); espacos e itens em branco sao descartados
 * @param inicio opcional: inicio do periodo de vigencia
 * @param fim    opcional: fim do periodo de vigencia, nunca anterior ao inicio
 */
public record DadosEstrategia(String titulo, String descricao, String categoria, String campanha,
        StatusEstrategia status, List<String> metas, LocalDate inicio, LocalDate fim) {

    public static final int MAXIMO_METAS = 8;

    public DadosEstrategia {
        titulo = Validacoes.exigirTexto(titulo, "título");
        descricao = Validacoes.exigirTexto(descricao, "descrição");
        categoria = Validacoes.exigirTexto(categoria, "categoria");
        campanha = Textos.ouNulo(campanha);
        metas = normalizar(metas);
        if (inicio != null && fim != null && fim.isBefore(inicio)) {
            throw new RegraDeNegocioException("O fim da estratégia não pode ser anterior ao início.");
        }
    }

    /** Estrategia sem metas nem periodo (o formato anterior ao detalhamento). */
    public DadosEstrategia(String titulo, String descricao, String categoria, String campanha,
            StatusEstrategia status) {
        this(titulo, descricao, categoria, campanha, status, List.of(), null, null);
    }

    private static List<String> normalizar(List<String> metas) {
        List<String> limpas = metas == null ? List.of() : metas.stream()
                .map(Textos::ouNulo)
                .filter(meta -> meta != null)
                .toList();
        if (limpas.size() > MAXIMO_METAS) {
            throw new RegraDeNegocioException("Informe no máximo " + MAXIMO_METAS + " metas.");
        }
        return limpas;
    }
}
