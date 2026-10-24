package br.com.fiap.aguiainova.service;

import java.time.Clock;
import java.time.Duration;

import br.com.fiap.aguiainova.config.AppProperties;
import br.com.fiap.aguiainova.dto.response.InsightsIaResponse;
import br.com.fiap.aguiainova.exception.LimiteDeRequisicoesExcedidoException;
import br.com.fiap.aguiainova.security.LimitadorDeTaxa;
import br.com.fiap.aguiainova.security.UsuarioLogado;
import br.com.fiap.aguiainova.service.ia.GeradorDeInsights;
import br.com.fiap.aguiainova.service.ia.IndicadoresDashboard;
import br.com.fiap.aguiainova.service.ia.InsightsGerados;
import org.springframework.stereotype.Service;

/** Insights e recomendacoes sobre os resultados do dashboard, para a lideranca. */
@Service
public class InsightsIaService {

    private static final String MENSAGEM_LIMITE_EXCEDIDO =
            "Muitas chamadas de IA em pouco tempo. Tente novamente em instantes.";

    private final GeradorDeInsights gerador;
    private final DashboardService dashboard;
    private final Clock clock;
    private final LimitadorDeTaxa limitador;
    private final int limiteIaPorMinuto;

    public InsightsIaService(GeradorDeInsights gerador, DashboardService dashboard, Clock clock,
            LimitadorDeTaxa limitador, AppProperties props) {
        this.gerador = gerador;
        this.dashboard = dashboard;
        this.clock = clock;
        this.limitador = limitador;
        this.limiteIaPorMinuto = props.rateLimit().iaPorMinuto();
    }

    public InsightsIaResponse gerar(UsuarioLogado usuario) {
        if (!limitador.permitir("ia:" + usuario.id(), limiteIaPorMinuto, Duration.ofMinutes(1))) {
            throw new LimiteDeRequisicoesExcedidoException(MENSAGEM_LIMITE_EXCEDIDO);
        }
        var indicadores = new IndicadoresDashboard(dashboard.resumo(null, null), dashboard.porEstrategia(),
                dashboard.porProjeto(null));
        InsightsGerados gerados = gerador.gerar(indicadores);
        return new InsightsIaResponse(gerados.resumo(), gerados.insights(), gerados.recomendacoes(), gerados.fonte(),
                clock.instant());
    }
}
