package br.com.fiap.aguiainova.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.com.fiap.aguiainova.model.enums.EtapaProjeto;
import br.com.fiap.aguiainova.model.enums.StatusProjeto;

public record DesempenhoProjetoResponse(
        String projetoId,
        String nome,
        StatusProjeto status,
        EtapaProjeto etapa,
        int progresso,
        LocalDate prazo,
        boolean atrasado,
        BigDecimal investimento,
        BigDecimal retorno,
        BigDecimal lucro,
        BigDecimal roiPercentual,
        Double ganhoProdutividade,
        Double reducaoCustos) {
}
