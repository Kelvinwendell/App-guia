package br.com.fiap.aguiainova.util;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Percentuais nos textos gerados por regras saem no formato brasileiro, igual ao que o app mostra ("13,1%"). */
class NumerosTest {

    @Test
    void usaVirgulaDecimal() {
        assertThat(Numeros.percentual(new BigDecimal("13.08"))).isEqualTo("13,08%");
    }

    @Test
    void naoMostraZerosInuteis() {
        assertThat(Numeros.percentual(new BigDecimal("-40.00"))).isEqualTo("-40%");
    }

    @Test
    void agrupaMilhares() {
        assertThat(Numeros.percentual(new BigDecimal("1250.5"))).isEqualTo("1.250,5%");
    }

    @Test
    void semValorFicaIndisponivel() {
        assertThat(Numeros.percentual(null)).isEqualTo("n/d");
    }
}
