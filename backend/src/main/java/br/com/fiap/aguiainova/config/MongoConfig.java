package br.com.fiap.aguiainova.config;

import java.time.LocalDate;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions.BigDecimalRepresentation;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

@Configuration
@EnableMongoAuditing
public class MongoConfig {

    /**
     * Por padrao o Spring Data grava LocalDate como Date no fuso da JVM, o que desloca o dia quando a
     * aplicacao roda em fusos diferentes (maquina local x container UTC). Gravamos como texto ISO-8601.
     * Valores monetarios sao gravados como Decimal128 para nao perder precisao nem o tipo numerico.
     */
    @Bean
    MongoCustomConversions mongoCustomConversions() {
        return MongoCustomConversions.create(adapter -> {
            adapter.registerConverter(LocalDateParaTexto.INSTANCE);
            adapter.registerConverter(TextoParaLocalDate.INSTANCE);
            adapter.bigDecimal(BigDecimalRepresentation.DECIMAL128);
        });
    }

    @WritingConverter
    enum LocalDateParaTexto implements Converter<LocalDate, String> {
        INSTANCE;

        @Override
        public String convert(LocalDate source) {
            return source.toString();
        }
    }

    @ReadingConverter
    enum TextoParaLocalDate implements Converter<String, LocalDate> {
        INSTANCE;

        @Override
        public LocalDate convert(String source) {
            return LocalDate.parse(source);
        }
    }
}
