package br.com.fiap.aguiainova.service.ia;

import br.com.fiap.aguiainova.integration.ai.LlmIndisponivelException;
import br.com.fiap.aguiainova.integration.ai.LlmNaoConfiguradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/** Decorator: usa a IA generativa e, se ela falhar ou nao estiver configurada, a leitura por regras. */
@Primary
@Component
public class GeradorInsightsComContingencia implements GeradorDeInsights {

    private static final Logger log = LoggerFactory.getLogger(GeradorInsightsComContingencia.class);

    private final GeradorDeInsights principal;
    private final GeradorDeInsights contingencia;

    public GeradorInsightsComContingencia(GeradorInsightsLlm principal, GeradorInsightsHeuristico contingencia) {
        this.principal = principal;
        this.contingencia = contingencia;
    }

    @Override
    public InsightsGerados gerar(IndicadoresDashboard indicadores) {
        try {
            return principal.gerar(indicadores);
        } catch (LlmNaoConfiguradoException e) {
            return contingencia.gerar(indicadores);
        } catch (LlmIndisponivelException e) {
            log.warn("IA indisponível para gerar insights; usando leitura por regras. Motivo: {}", e.getMessage());
            return contingencia.gerar(indicadores);
        }
    }
}
