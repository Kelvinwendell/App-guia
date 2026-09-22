package br.com.fiap.aguiainova.service;

import java.time.Clock;

import br.com.fiap.aguiainova.dto.response.InsightsIaResponse;
import br.com.fiap.aguiainova.service.ia.GeradorDeInsights;
import br.com.fiap.aguiainova.service.ia.IndicadoresDashboard;
import br.com.fiap.aguiainova.service.ia.InsightsGerados;
import org.springframework.stereotype.Service;

/** Insights e recomendacoes sobre os resultados do dashboard, para a lideranca. */
@Service
public class InsightsIaService {

    private final GeradorDeInsights gerador;
    private final DashboardService dashboard;
    private final Clock clock;

    public InsightsIaService(GeradorDeInsights gerador, DashboardService dashboard, Clock clock) {
        this.gerador = gerador;
        this.dashboard = dashboard;
        this.clock = clock;
    }

    public InsightsIaResponse gerar() {
        var indicadores = new IndicadoresDashboard(dashboard.resumo(null, null), dashboard.porEstrategia(),
                dashboard.porProjeto(null));
        InsightsGerados gerados = gerador.gerar(indicadores);
        return new InsightsIaResponse(gerados.resumo(), gerados.insights(), gerados.recomendacoes(), gerados.fonte(),
                clock.instant());
    }
}
