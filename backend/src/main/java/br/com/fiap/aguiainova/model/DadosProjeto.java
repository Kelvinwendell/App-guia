package br.com.fiap.aguiainova.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.com.fiap.aguiainova.exception.RegraDeNegocioException;
import br.com.fiap.aguiainova.model.enums.EtapaProjeto;
import br.com.fiap.aguiainova.model.enums.StatusProjeto;
import br.com.fiap.aguiainova.util.Textos;
import br.com.fiap.aguiainova.util.Validacoes;

/**
 * Dados cadastrais de um projeto, ja validados: investimento nao negativo e prazo nao anterior ao inicio.
 *
 * @param status opcional: na criacao o padrao e PLANEJAMENTO; na atualizacao, se nulo, o atual e mantido
 * @param etapa  opcional: na criacao o padrao e INICIACAO; na atualizacao, se nula, a atual e mantida
 */
public record DadosProjeto(String nome, String descricao, BigDecimal investimento, LocalDate dataInicio,
        LocalDate prazo, String estrategiaId, String ideiaId, StatusProjeto status, EtapaProjeto etapa) {

    public DadosProjeto {
        nome = Validacoes.exigirTexto(nome, "nome");
        descricao = Validacoes.exigirTexto(descricao, "descrição");
        investimento = Validacoes.exigir(investimento, "investimento");
        dataInicio = Validacoes.exigir(dataInicio, "data de início");
        prazo = Validacoes.exigir(prazo, "prazo");
        estrategiaId = Textos.ouNulo(estrategiaId);
        ideiaId = Textos.ouNulo(ideiaId);
        if (investimento.signum() < 0) {
            throw new RegraDeNegocioException("O investimento não pode ser negativo.");
        }
        if (prazo.isBefore(dataInicio)) {
            throw new RegraDeNegocioException("O prazo não pode ser anterior à data de início.");
        }
    }
}
