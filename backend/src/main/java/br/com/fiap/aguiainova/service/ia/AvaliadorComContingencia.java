package br.com.fiap.aguiainova.service.ia;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.fiap.aguiainova.integration.ai.LlmIndisponivelException;
import br.com.fiap.aguiainova.integration.ai.LlmNaoConfiguradoException;
import br.com.fiap.aguiainova.model.AvaliacaoIa;
import br.com.fiap.aguiainova.model.Estrategia;
import br.com.fiap.aguiainova.model.Ideia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Decorator que garante uma nota para toda ideia: tenta o avaliador principal (IA generativa) e entrega ao de
 * contingencia (regras) as ideias que ele nao conseguiu avaliar. E o avaliador injetado por padrao.
 */
@Primary
@Component
public class AvaliadorComContingencia implements AvaliadorDeIdeias {

    private static final Logger log = LoggerFactory.getLogger(AvaliadorComContingencia.class);

    private final AvaliadorDeIdeias principal;
    private final AvaliadorDeIdeias contingencia;

    public AvaliadorComContingencia(AvaliadorLlm principal, AvaliadorHeuristico contingencia) {
        this.principal = principal;
        this.contingencia = contingencia;
    }

    @Override
    public Map<String, AvaliacaoIa> avaliar(List<Ideia> ideias, List<Estrategia> estrategiasVigentes) {
        Map<String, AvaliacaoIa> avaliacoes = new HashMap<>(tentarPrincipal(ideias, estrategiasVigentes));
        List<Ideia> semNota = ideias.stream().filter(ideia -> !avaliacoes.containsKey(ideia.getId())).toList();
        if (!semNota.isEmpty()) {
            avaliacoes.putAll(contingencia.avaliar(semNota, estrategiasVigentes));
        }
        return avaliacoes;
    }

    private Map<String, AvaliacaoIa> tentarPrincipal(List<Ideia> ideias, List<Estrategia> estrategiasVigentes) {
        try {
            return principal.avaliar(ideias, estrategiasVigentes);
        } catch (LlmNaoConfiguradoException e) {
            return Map.of();
        } catch (LlmIndisponivelException e) {
            log.warn("IA indisponível para pontuar ideias; usando avaliação por regras. Motivo: {}", e.getMessage());
            return Map.of();
        }
    }
}
