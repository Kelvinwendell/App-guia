package br.com.fiap.aguiainova.service;

import br.com.fiap.aguiainova.config.AppProperties;
import br.com.fiap.aguiainova.dto.response.StatusIaResponse;
import br.com.fiap.aguiainova.integration.ai.LlmClient;
import org.springframework.stereotype.Service;

@Service
public class StatusIaService {

    private final LlmClient llm;
    private final AppProperties props;

    public StatusIaService(LlmClient llm, AppProperties props) {
        this.llm = llm;
        this.props = props;
    }

    /** Permite ao app decidir o que exibir (chat, selo "IA" ou aviso de avaliação por regras). */
    public StatusIaResponse status() {
        return new StatusIaResponse(props.ai().enabled(), llm.disponivel(), llm.provedor(), llm.modelo());
    }
}
