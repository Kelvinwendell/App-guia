package br.com.fiap.aguiainova.service;

import java.util.List;

import br.com.fiap.aguiainova.dto.request.ChatIaRequest;
import br.com.fiap.aguiainova.dto.request.MensagemChatRequest;
import br.com.fiap.aguiainova.dto.response.ChatIaResponse;
import br.com.fiap.aguiainova.exception.ServicoIndisponivelException;
import br.com.fiap.aguiainova.integration.ai.LlmClient;
import br.com.fiap.aguiainova.integration.ai.LlmIndisponivelException;
import br.com.fiap.aguiainova.integration.ai.LlmNaoConfiguradoException;
import br.com.fiap.aguiainova.integration.ai.MensagemLlm;
import br.com.fiap.aguiainova.model.enums.StatusEstrategia;
import br.com.fiap.aguiainova.model.enums.StatusIdeia;
import br.com.fiap.aguiainova.repository.EstrategiaRepository;
import br.com.fiap.aguiainova.repository.IdeiaRepository;
import br.com.fiap.aguiainova.repository.ProjetoRepository;
import br.com.fiap.aguiainova.service.ia.ContextoAssistente;
import br.com.fiap.aguiainova.service.ia.PromptsIa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * Chat com IA generativa que ajuda o gestor a escolher ideias e estruturar projetos. Diferente da pontuacao, uma
 * conversa nao tem substituto por regras: sem IA disponivel, responde 503.
 */
@Service
public class AssistenteIaService {

    private static final Logger log = LoggerFactory.getLogger(AssistenteIaService.class);
    private static final int IDEIAS_EM_ANALISE_NO_CONTEXTO = 15;
    private static final int IDEIAS_APROVADAS_NO_CONTEXTO = 10;
    private static final int PROJETOS_NO_CONTEXTO = 10;
    private static final Sort MAIS_RECENTES = Sort.by(Sort.Direction.DESC, "criadoEm");

    private final LlmClient llm;
    private final PromptsIa prompts;
    private final IdeiaRepository ideias;
    private final EstrategiaRepository estrategias;
    private final ProjetoRepository projetos;

    public AssistenteIaService(LlmClient llm, PromptsIa prompts, IdeiaRepository ideias,
            EstrategiaRepository estrategias, ProjetoRepository projetos) {
        this.llm = llm;
        this.prompts = prompts;
        this.ideias = ideias;
        this.estrategias = estrategias;
        this.projetos = projetos;
    }

    public ChatIaResponse conversar(ChatIaRequest req) {
        List<MensagemLlm> conversa = prompts.montarConversa(historicoDe(req), req.mensagem());
        try {
            String resposta = llm.gerar(prompts.assistenteDoGestor(montarContexto(), conversa));
            return new ChatIaResponse(resposta.strip(), llm.provedor());
        } catch (LlmNaoConfiguradoException e) {
            throw new ServicoIndisponivelException("O assistente de IA não está configurado neste ambiente.");
        } catch (LlmIndisponivelException e) {
            log.warn("Assistente de IA indisponível. Motivo: {}", e.getMessage());
            throw new ServicoIndisponivelException(
                    "O assistente de IA está indisponível no momento. Tente novamente em instantes.");
        }
    }

    private static List<MensagemLlm> historicoDe(ChatIaRequest req) {
        if (req.historico() == null) {
            return List.of();
        }
        return req.historico().stream().map(AssistenteIaService::paraMensagem).toList();
    }

    private static MensagemLlm paraMensagem(MensagemChatRequest mensagem) {
        return new MensagemLlm(mensagem.papel(), mensagem.texto());
    }

    private ContextoAssistente montarContexto() {
        return new ContextoAssistente(
                estrategias.findByStatus(StatusEstrategia.VIGENTE),
                ideias.findByStatus(StatusIdeia.EM_ANALISE,
                        PageRequest.of(0, IDEIAS_EM_ANALISE_NO_CONTEXTO, MAIS_RECENTES)),
                ideias.findByStatus(StatusIdeia.APROVADA,
                        PageRequest.of(0, IDEIAS_APROVADAS_NO_CONTEXTO, MAIS_RECENTES)),
                projetos.findAll(PageRequest.of(0, PROJETOS_NO_CONTEXTO, MAIS_RECENTES)).getContent());
    }
}
