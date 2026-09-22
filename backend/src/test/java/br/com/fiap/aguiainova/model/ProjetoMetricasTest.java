package br.com.fiap.aguiainova.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import br.com.fiap.aguiainova.support.Dados;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProjetoMetricasTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 6, 15);

    private Projeto projeto(String investimento) {
        return Dados.projeto(new BigDecimal(investimento), HOJE, HOJE.plusDays(30), null);
    }

    private void resultado(Projeto projeto, String retorno, Double produtividade, Double custos) {
        projeto.registrarResultado(new DadosResultado(HOJE, "Marco", new BigDecimal(retorno), produtividade, custos),
                Dados.AUTOR);
    }

    @Test
    void projetoSemResultadosTemRetornoZeroELucroNegativo() {
        ProjetoMetricas metricas = ProjetoMetricas.de(projeto("1000"));

        assertThat(metricas.retornoFinanceiro()).isEqualByComparingTo("0");
        assertThat(metricas.lucro()).isEqualByComparingTo("-1000");
        assertThat(metricas.roi()).isEqualByComparingTo("-100.00");
        assertThat(metricas.ganhoProdutividade()).isNull();
        assertThat(metricas.reducaoCustos()).isNull();
    }

    @Test
    void somaOsResultadosECalculaLucroERoi() {
        Projeto projeto = projeto("40000");
        resultado(projeto, "30000", null, null);
        resultado(projeto, "30000", null, null);

        ProjetoMetricas metricas = ProjetoMetricas.de(projeto);

        assertThat(metricas.retornoFinanceiro()).isEqualByComparingTo("60000");
        assertThat(metricas.lucro()).isEqualByComparingTo("20000");
        assertThat(metricas.roi()).isEqualByComparingTo("50.00");
    }

    @Test
    void naoPerdePrecisaoComCentavos() {
        Projeto projeto = projeto("0.10");
        resultado(projeto, "0.20", null, null);

        assertThat(ProjetoMetricas.de(projeto).lucro()).isEqualByComparingTo("0.10");
    }

    @Test
    void mediasIgnoramResultadosQueNaoInformaramOValor() {
        Projeto projeto = projeto("100");
        resultado(projeto, "1", 10.0, 4.0);
        resultado(projeto, "1", 15.0, null);
        resultado(projeto, "1", null, null);

        ProjetoMetricas metricas = ProjetoMetricas.de(projeto);

        assertThat(metricas.ganhoProdutividade()).isEqualTo(12.5);
        assertThat(metricas.reducaoCustos()).isEqualTo(4.0);
    }

    @Test
    void roiNaoExisteSemInvestimento() {
        assertThat(ProjetoMetricas.roi(new BigDecimal("500"), BigDecimal.ZERO)).isNull();
        assertThat(ProjetoMetricas.roi(new BigDecimal("500"), null)).isNull();
    }

    @Test
    void mediaArredondadaTemDuasCasasEEhNulaSemValores() {
        assertThat(ProjetoMetricas.mediaArredondada(List.of(1.0, 2.0, 2.0), v -> v)).isEqualTo(1.67);
        assertThat(ProjetoMetricas.mediaArredondada(List.<Double>of(), v -> v)).isNull();
    }
}
