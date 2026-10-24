package br.com.fiap.aguiainova.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import br.com.fiap.aguiainova.model.enums.NivelSaude;

/** Saúde de uma estratégia vigente: o nível apurado por regras, o porquê, a próxima ação e os números usados. */
public record SaudeEstrategiaResponse(
        String estrategiaId,
        String titulo,
        String categoria,
        NivelSaude nivel,
        List<String> motivos,
        String proximaAcao,
        long ideias,
        long ideiasEmAnalise,
        long aprovadasSemProjeto,
        int projetos,
        int projetosAtrasados,
        BigDecimal investimento,
        BigDecimal retorno,
        /** Nulo quando não há investimento. */
        BigDecimal roiPercentual,
        /** Nulo quando nada está vinculado à estratégia. */
        LocalDate ultimaAtividade) {
}
