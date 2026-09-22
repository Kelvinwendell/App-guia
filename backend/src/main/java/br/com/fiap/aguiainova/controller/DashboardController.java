package br.com.fiap.aguiainova.controller;

import java.util.List;

import br.com.fiap.aguiainova.dto.response.DesempenhoEstrategiaResponse;
import br.com.fiap.aguiainova.dto.response.DesempenhoProjetoResponse;
import br.com.fiap.aguiainova.dto.response.EvolucaoMensalResponse;
import br.com.fiap.aguiainova.dto.response.ResumoDashboardResponse;
import br.com.fiap.aguiainova.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasRole('LIDERANCA')")
@Tag(name = "Dashboard", description = "Relatórios resumidos para a liderança (ROI, lucro, prazo, investimento, produtividade)")
public class DashboardController {

    private final DashboardService dashboard;

    public DashboardController(DashboardService dashboard) {
        this.dashboard = dashboard;
    }

    @GetMapping("/resumo")
    @Operation(summary = "Resumo geral; filtre por estratégia e/ou projeto para ver retornos específicos")
    public ResumoDashboardResponse resumo(@RequestParam(required = false) String estrategiaId,
            @RequestParam(required = false) String projetoId) {
        return dashboard.resumo(estrategiaId, projetoId);
    }

    @GetMapping("/estrategias")
    @Operation(summary = "Retorno financeiro agregado por estratégia")
    public List<DesempenhoEstrategiaResponse> porEstrategia() {
        return dashboard.porEstrategia();
    }

    @GetMapping("/projetos")
    @Operation(summary = "Desempenho de cada projeto (opcionalmente de uma estratégia)")
    public List<DesempenhoProjetoResponse> porProjeto(@RequestParam(required = false) String estrategiaId) {
        return dashboard.porProjeto(estrategiaId);
    }

    @GetMapping("/evolucao")
    @Operation(summary = "Série mensal do retorno financeiro (para gráfico de linha)")
    public List<EvolucaoMensalResponse> evolucao(@RequestParam(required = false) String estrategiaId,
            @RequestParam(required = false) String projetoId) {
        return dashboard.evolucaoMensal(estrategiaId, projetoId);
    }
}
