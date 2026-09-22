package br.com.fiap.aguiainova;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AguiaInovaApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AguiaInovaApiApplication.class, args);
    }

}
