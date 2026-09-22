package br.com.fiap.aguiainova.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import br.com.fiap.aguiainova.model.enums.EtapaProjeto;
import br.com.fiap.aguiainova.model.enums.StatusProjeto;

public record ProjetoResponse(
        String id,
        String nome,
        String descricao,
        StatusProjeto status,
        EtapaProjeto etapa,
        BigDecimal investimento,
        LocalDate dataInicio,
        LocalDate prazo,
        int progresso,
        String estrategiaId,
        String estrategiaTitulo,
        String ideiaId,
        String ideiaTitulo,
        String autorId,
        String autorNome,
        /** Soma dos resultados registrados. */
        BigDecimal retornoFinanceiro,
        BigDecimal lucro,
        /** ROI em %, nulo quando não há investimento. */
        BigDecimal roi,
        Double ganhoProdutividade,
        Double reducaoCustos,
        boolean atrasado,
        /** Dias até o prazo (negativo = já passou). */
        long diasParaPrazo,
        List<ResultadoProjetoResponse> resultados,
        Instant criadoEm,
        Instant atualizadoEm) {
}
