package br.com.fiap.aguiainova.service.ia;

import br.com.fiap.aguiainova.integration.ai.LlmClient;
import br.com.fiap.aguiainova.model.DiagnosticoEstrategia;
import org.springframework.stereotype.Component;

/** Explica a saude da estrategia com o modelo de linguagem; o nivel continua sendo o das regras. */
@Component
public class LeitorSaudeLlm implements LeitorDeSaude {

    private final LlmClient llm;
    private final PromptsIa prompts;
    private final InterpretadorRespostaIa interpretador;

    public LeitorSaudeLlm(LlmClient llm, PromptsIa prompts, InterpretadorRespostaIa interpretador) {
        this.llm = llm;
        this.prompts = prompts;
        this.interpretador = interpretador;
    }

    /** @throws br.com.fiap.aguiainova.integration.ai.LlmIndisponivelException se o modelo nao puder responder */
    @Override
    public LeituraGerada ler(DiagnosticoEstrategia diagnostico) {
        String resposta = llm.gerar(prompts.leituraDeSaude(diagnostico));
        return interpretador.interpretarLeitura(resposta, llm.provedor());
    }
}
