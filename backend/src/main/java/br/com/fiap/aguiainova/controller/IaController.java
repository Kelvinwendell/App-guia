package br.com.fiap.aguiainova.controller;

import br.com.fiap.aguiainova.dto.request.AnaliseEstrategiaRequest;
import br.com.fiap.aguiainova.dto.request.ChatIaRequest;
import br.com.fiap.aguiainova.dto.request.RascunhoEstrategiaRequest;
import br.com.fiap.aguiainova.dto.response.AnaliseEstrategiaResponse;
import br.com.fiap.aguiainova.dto.response.AvaliacaoIaResponse;
import br.com.fiap.aguiainova.dto.response.ChatIaResponse;
import br.com.fiap.aguiainova.dto.response.InsightsIaResponse;
import br.com.fiap.aguiainova.dto.response.LeituraSaudeResponse;
import br.com.fiap.aguiainova.dto.response.RankingIaResponse;
import br.com.fiap.aguiainova.dto.response.RascunhoEstrategiaResponse;
import br.com.fiap.aguiainova.dto.response.StatusIaResponse;
import br.com.fiap.aguiainova.service.AssistenteIaService;
import br.com.fiap.aguiainova.service.AvaliacaoIaService;
import br.com.fiap.aguiainova.service.EstrategiaIaService;
import br.com.fiap.aguiainova.service.InsightsIaService;
import br.com.fiap.aguiainova.service.StatusIaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ia")
@Tag(name = "Inteligência Artificial", description = "Pontuação de ideias, assistente do gestor e insights do dashboard")
public class IaController {

    private final StatusIaService status;
    private final AvaliacaoIaService avaliacao;
    private final AssistenteIaService assistente;
    private final InsightsIaService insights;
    private final EstrategiaIaService estrategias;

    public IaController(StatusIaService status, AvaliacaoIaService avaliacao, AssistenteIaService assistente,
            InsightsIaService insights, EstrategiaIaService estrategias) {
        this.status = status;
        this.avaliacao = avaliacao;
        this.assistente = assistente;
        this.insights = insights;
        this.estrategias = estrategias;
    }

    @GetMapping("/status")
    @Operation(summary = "Informa se a IA generativa está disponível neste ambiente")
    public StatusIaResponse status() {
        return status.status();
    }

    @PostMapping("/ideias/{id}/avaliar")
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Pontua uma ideia (0-100) e sugere prioridade (GESTOR)")
    public AvaliacaoIaResponse avaliarIdeia(@PathVariable String id) {
        return avaliacao.avaliar(id);
    }

    @PostMapping("/ideias/avaliar-pendentes")
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Pontua as ideias em análise ainda sem nota e devolve o ranking (GESTOR)")
    public RankingIaResponse avaliarPendentes() {
        return avaliacao.avaliarPendentes();
    }

    @PostMapping("/chat")
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Conversa com o assistente de IA para escolher ideias e estruturar projetos (GESTOR)")
    public ChatIaResponse conversar(@Valid @RequestBody ChatIaRequest req) {
        return assistente.conversar(req);
    }

    @PostMapping("/estrategias/rascunho")
    @PreAuthorize("hasRole('LIDERANCA')")
    @Operation(summary = "Rascunho de estratégia (título, descrição, metas e período) a partir de um objetivo; "
            + "503 sem IA (LIDERANCA)")
    public RascunhoEstrategiaResponse rascunharEstrategia(@Valid @RequestBody RascunhoEstrategiaRequest req) {
        return estrategias.rascunhar(req);
    }

    @PostMapping("/estrategias/analise")
    @PreAuthorize("hasRole('LIDERANCA')")
    @Operation(summary = "Critica uma estratégia antes de salvar: nota, melhorias e sobreposição (LIDERANCA)")
    public AnaliseEstrategiaResponse analisarEstrategia(@Valid @RequestBody AnaliseEstrategiaRequest req) {
        return estrategias.analisar(req);
    }

    @PostMapping("/estrategias/{id}/saude")
    @PreAuthorize("hasAnyRole('GESTOR','LIDERANCA')")
    @Operation(summary = "Lê a saúde de uma estratégia vigente: explicação e recomendações (GESTOR, LIDERANCA)")
    public LeituraSaudeResponse lerSaudeDaEstrategia(@PathVariable String id) {
        return estrategias.lerSaude(id);
    }

    @PostMapping("/dashboard/insights")
    @PreAuthorize("hasRole('LIDERANCA')")
    @Operation(summary = "Gera insights e recomendações da IA sobre os resultados do dashboard (LIDERANCA)")
    public InsightsIaResponse gerarInsights() {
        return insights.gerar();
    }
}
