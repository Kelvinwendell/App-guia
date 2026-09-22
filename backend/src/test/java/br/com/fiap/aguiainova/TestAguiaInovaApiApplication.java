package br.com.fiap.aguiainova;

import org.springframework.boot.SpringApplication;

public class TestAguiaInovaApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(AguiaInovaApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
