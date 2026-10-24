package br.com.fiap.aguiainova.config;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class SeedDemoPropertiesTest {

    @ParameterizedTest
    @ValueSource(strings = {"carregar", "CARREGAR", "  carregar  ", "true", "TRUE", "sim", "1"})
    void reconheceOsModosDeCarga(String modo) {
        var demo = new AppProperties.Seed.Demo(modo, "");

        assertThat(demo.carregar()).isTrue();
        assertThat(demo.remover()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"remover", "REMOVER", " limpar "})
    void reconheceOsModosDeRemocao(String modo) {
        var demo = new AppProperties.Seed.Demo(modo, "");

        assertThat(demo.remover()).isTrue();
        assertThat(demo.carregar()).isFalse();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "desligado", "false", "0", "nao", "qualquer-coisa"})
    void qualquerOutroValorDeixaTudoDesligado(String modo) {
        var demo = new AppProperties.Seed.Demo(modo, "senha");

        assertThat(demo.carregar()).isFalse();
        assertThat(demo.remover()).isFalse();
    }
}
