package br.com.fiap.aguiainova.service.ia;

import br.com.fiap.aguiainova.integration.ai.LlmIndisponivelException;
import br.com.fiap.aguiainova.integration.ai.LlmNaoConfiguradoException;
import br.com.fiap.aguiainova.model.DiagnosticoEstrategia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/** Decorator: usa a IA generativa e, se ela falhar ou nao estiver configurada, a leitura por regras. */
@Primary
@Component
public class LeitorSaudeComContingencia implements LeitorDeSaude {

    private static final Logger log = LoggerFactory.getLogger(LeitorSaudeComContingencia.class);

    private final LeitorDeSaude principal;
    private final LeitorDeSaude contingencia;

    public LeitorSaudeComContingencia(LeitorSaudeLlm principal, LeitorSaudeHeuristico contingencia) {
        this.principal = principal;
        this.contingencia = contingencia;
    }

    @Override
    public LeituraGerada ler(DiagnosticoEstrategia diagnostico) {
        try {
            return principal.ler(diagnostico);
        } catch (LlmNaoConfiguradoException e) {
            return contingencia.ler(diagnostico);
        } catch (LlmIndisponivelException e) {
            log.warn("IA indisponível para ler a saúde da estratégia; usando regras. Motivo: {}", e.getMessage());
            return contingencia.ler(diagnostico);
        }
    }
}
