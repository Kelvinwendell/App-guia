package br.com.fiap.aguiainova.service.ia;

import java.util.List;

import br.com.fiap.aguiainova.model.Estrategia;
import br.com.fiap.aguiainova.model.Ideia;
import br.com.fiap.aguiainova.model.Projeto;

/** Retrato dos dados da empresa entregue ao assistente para fundamentar as respostas. */
public record ContextoAssistente(
        List<Estrategia> estrategiasVigentes,
        List<Ideia> ideiasEmAnalise,
        List<Ideia> ideiasAprovadas,
        List<Projeto> projetos) {
}
