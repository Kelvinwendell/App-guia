package br.com.fiap.aguiainova.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import br.com.fiap.aguiainova.model.enums.EtapaProjeto;
import br.com.fiap.aguiainova.model.enums.StatusProjeto;

/**
 * Visão geral para a liderança. Os indicadores financeiros (investimento, retorno, lucro, ROI, produtividade e
 * custos) consideram os projetos não cancelados; as contagens por status e etapa consideram todos.
 */
public record ResumoDashboardResponse(
        BigDecimal investimentoTotal,
        BigDecimal retornoTotal,
        BigDecimal lucroTotal,
        /** ROI geral em %, nulo quando não há investimento. */
        BigDecimal roiPercentual,
        /** Média do aumento de produtividade (%) dos projetos que o informaram. */
        Double ganhoProdutividadeMedio,
        /** Média da redução de custos (%) dos projetos que a informaram. */
        Double reducaoCustosMedia,
        int totalProjetos,
        int projetosAtrasados,
        Map<StatusProjeto, Long> projetosPorStatus,
        Map<EtapaProjeto, Long> projetosPorEtapa,
        ResumoIdeiasResponse ideias,
        List<PrazoProjetoResponse> proximosPrazos) {
}
