package br.com.fiap.aguiainova.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LimitadorDeTaxaTest {

    /** Relogio de teste cujo instante pode ser avançado manualmente, para simular a janela expirando. */
    private static final class RelogioControlavel extends Clock {
        private Instant agora;

        RelogioControlavel(Instant inicial) {
            this.agora = inicial;
        }

        void avancar(Duration duracao) {
            agora = agora.plus(duracao);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return agora;
        }
    }

    private final RelogioControlavel relogio = new RelogioControlavel(Instant.parse("2026-01-01T00:00:00Z"));
    private final LimitadorDeTaxa limitador = new LimitadorDeTaxa(relogio);

    @Test
    void permiteAteOLimiteDentroDaJanela() {
        for (int i = 0; i < 3; i++) {
            assertThat(limitador.permitir("chave", 3, Duration.ofMinutes(1))).isTrue();
        }
    }

    @Test
    void bloqueiaAPartirDoLimiteDentroDaMesmaJanela() {
        for (int i = 0; i < 3; i++) {
            limitador.permitir("chave", 3, Duration.ofMinutes(1));
        }

        assertThat(limitador.permitir("chave", 3, Duration.ofMinutes(1))).isFalse();
    }

    @Test
    void chavesDiferentesTemContadoresIndependentes() {
        for (int i = 0; i < 3; i++) {
            limitador.permitir("chave-a", 3, Duration.ofMinutes(1));
        }

        assertThat(limitador.permitir("chave-b", 3, Duration.ofMinutes(1))).isTrue();
    }

    @Test
    void liberaDeNovoQuandoAJanelaExpira() {
        for (int i = 0; i < 3; i++) {
            limitador.permitir("chave", 3, Duration.ofMinutes(1));
        }
        assertThat(limitador.permitir("chave", 3, Duration.ofMinutes(1))).isFalse();

        relogio.avancar(Duration.ofMinutes(1).plusSeconds(1));

        assertThat(limitador.permitir("chave", 3, Duration.ofMinutes(1))).isTrue();
    }
}
