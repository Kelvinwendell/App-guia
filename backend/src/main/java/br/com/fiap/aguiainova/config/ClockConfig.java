package br.com.fiap.aguiainova.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

    public static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    /** Relogio unico da aplicacao: "hoje" (prazos, atrasos) e sempre o dia de Brasilia, independente da JVM. */
    @Bean
    Clock clock() {
        return Clock.system(FUSO);
    }
}
