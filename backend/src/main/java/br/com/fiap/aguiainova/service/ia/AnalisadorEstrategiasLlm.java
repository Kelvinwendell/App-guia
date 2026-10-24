package br.com.fiap.aguiainova.service.ia;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import br.com.fiap.aguiainova.integration.ai.LlmClient;
import br.com.fiap.aguiainova.model.DadosEstrategia;
import br.com.fiap.aguiainova.model.Estrategia;
import org.springframework.stereotype.Component;

/** Critica a estrategia com o modelo de linguagem. */
@Component
public class AnalisadorEstrategiasLlm implements AnalisadorDeEstrategias {

    private final LlmClient llm;
    private final PromptsIa prompts;
    private final InterpretadorRespostaIa interpretador;

    public AnalisadorEstrategiasLlm(LlmClient llm, PromptsIa prompts, InterpretadorRespostaIa interpretador) {
        this.llm = llm;
        this.prompts = prompts;
        this.interpretador = interpretador;
    }

    /** @throws br.com.fiap.aguiainova.integration.ai.LlmIndisponivelException se o modelo nao puder responder */
    @Override
    public AnaliseGerada analisar(DadosEstrategia dados, List<Estrategia> outrasVigentes) {
        Set<String> idsConhecidos = outrasVigentes.stream().map(Estrategia::getId).collect(Collectors.toSet());
        String resposta = llm.gerar(prompts.analiseDeEstrategia(dados, outrasVigentes));
        return interpretador.interpretarAnalise(resposta, llm.provedor(), idsConhecidos);
    }
}
