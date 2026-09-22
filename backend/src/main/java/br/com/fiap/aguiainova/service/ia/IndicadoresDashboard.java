package br.com.fiap.aguiainova.service.ia;

import java.util.List;

import br.com.fiap.aguiainova.dto.response.DesempenhoEstrategiaResponse;
import br.com.fiap.aguiainova.dto.response.DesempenhoProjetoResponse;
import br.com.fiap.aguiainova.dto.response.ResumoDashboardResponse;

/** Tudo o que o dashboard mostra a lideranca; e a entrada de qualquer gerador de insights. */
public record IndicadoresDashboard(
        ResumoDashboardResponse resumo,
        List<DesempenhoEstrategiaResponse> porEstrategia,
        List<DesempenhoProjetoResponse> porProjeto) {
}
