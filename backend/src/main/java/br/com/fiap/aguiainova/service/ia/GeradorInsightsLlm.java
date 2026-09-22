package br.com.fiap.aguiainova.service.ia;

import br.com.fiap.aguiainova.integration.ai.LlmClient;
import org.springframework.stereotype.Component;

/** Gera insights e recomendacoes com o modelo de linguagem. */
@Component
public class GeradorInsightsLlm implements GeradorDeInsights {

    private final LlmClient llm;
    private final PromptsIa prompts;
    private final InterpretadorRespostaIa interpretador;

    public GeradorInsightsLlm(LlmClient llm, PromptsIa prompts, InterpretadorRespostaIa interpretador) {
        this.llm = llm;
        this.prompts = prompts;
        this.interpretador = interpretador;
    }

    /** @throws br.com.fiap.aguiainova.integration.ai.LlmIndisponivelException se o modelo nao puder responder */
    @Override
    public InsightsGerados gerar(IndicadoresDashboard indicadores) {
        String resposta = llm.gerar(prompts.insightsDoDashboard(indicadores));
        return interpretador.interpretarInsights(resposta, llm.provedor());
    }
}
