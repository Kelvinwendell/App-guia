package br.com.fiap.aguiainova.service.ia;

import java.time.Clock;
import java.util.List;
import java.util.Map;

import br.com.fiap.aguiainova.integration.ai.LlmClient;
import br.com.fiap.aguiainova.model.AvaliacaoIa;
import br.com.fiap.aguiainova.model.Estrategia;
import br.com.fiap.aguiainova.model.Ideia;
import org.springframework.stereotype.Component;

/** Pontua as ideias com o modelo de linguagem, todas em uma unica chamada. */
@Component
public class AvaliadorLlm implements AvaliadorDeIdeias {

    private final LlmClient llm;
    private final PromptsIa prompts;
    private final InterpretadorRespostaIa interpretador;
    private final Clock clock;

    public AvaliadorLlm(LlmClient llm, PromptsIa prompts, InterpretadorRespostaIa interpretador, Clock clock) {
        this.llm = llm;
        this.prompts = prompts;
        this.interpretador = interpretador;
        this.clock = clock;
    }

    /** @throws br.com.fiap.aguiainova.integration.ai.LlmIndisponivelException se o modelo nao puder responder */
    @Override
    public Map<String, AvaliacaoIa> avaliar(List<Ideia> ideias, List<Estrategia> estrategiasVigentes) {
        String resposta = llm.gerar(prompts.avaliacaoDeIdeias(ideias, estrategiasVigentes));
        return interpretador.interpretarAvaliacoes(resposta, llm.provedor(), clock.instant());
    }
}
