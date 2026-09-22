package br.com.fiap.aguiainova.service.ia;

import java.util.List;
import java.util.Map;

import br.com.fiap.aguiainova.model.AvaliacaoIa;
import br.com.fiap.aguiainova.model.Estrategia;
import br.com.fiap.aguiainova.model.Ideia;

/**
 * Estrategia de pontuacao de ideias. Quem usa so conhece este contrato: se a nota vem de um modelo de linguagem,
 * de regras locais ou de uma combinacao dos dois e um detalhe de cada implementacao.
 */
public interface AvaliadorDeIdeias {

    /**
     * Pontua as ideias em relacao as estrategias vigentes.
     *
     * @return as avaliacoes que conseguiu produzir, indexadas pelo id da ideia (pode ser um subconjunto)
     */
    Map<String, AvaliacaoIa> avaliar(List<Ideia> ideias, List<Estrategia> estrategiasVigentes);
}
