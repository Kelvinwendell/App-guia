package br.com.fiap.aguiainova.service.ia;

import java.util.List;

import br.com.fiap.aguiainova.integration.ai.LlmIndisponivelException;
import br.com.fiap.aguiainova.integration.ai.LlmNaoConfiguradoException;
import br.com.fiap.aguiainova.model.DadosEstrategia;
import br.com.fiap.aguiainova.model.Estrategia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/** Decorator: critica com a IA generativa e, se ela falhar ou nao estiver configurada, com as regras. */
@Primary
@Component
public class AnalisadorEstrategiasComContingencia implements AnalisadorDeEstrategias {

    private static final Logger log = LoggerFactory.getLogger(AnalisadorEstrategiasComContingencia.class);

    private final AnalisadorDeEstrategias principal;
    private final AnalisadorDeEstrategias contingencia;

    public AnalisadorEstrategiasComContingencia(AnalisadorEstrategiasLlm principal,
            AnalisadorEstrategiasHeuristico contingencia) {
        this.principal = principal;
        this.contingencia = contingencia;
    }

    @Override
    public AnaliseGerada analisar(DadosEstrategia dados, List<Estrategia> outrasVigentes) {
        try {
            return principal.analisar(dados, outrasVigentes);
        } catch (LlmNaoConfiguradoException e) {
            return contingencia.analisar(dados, outrasVigentes);
        } catch (LlmIndisponivelException e) {
            log.warn("IA indisponível para analisar a estratégia; usando regras. Motivo: {}", e.getMessage());
            return contingencia.analisar(dados, outrasVigentes);
        }
    }
}
