package br.com.fiap.aguiainova.bootstrap;

import br.com.fiap.aguiainova.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sem nenhuma variavel de ambiente/propriedade definida, o seed NAO deve rodar: esquecer
 * APP_SEED_ENABLED em producao nao pode, por si so, criar contas de senha conhecida no banco.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class DataSeederDesabilitadoPorPadraoIT {

    @Autowired
    private ApplicationContext contexto;
    @Autowired
    private MongoTemplate mongo;

    @Test
    void semConfiguracaoExplicitaOSeedNaoRodaNemCriaUsuarios() {
        assertThat(contexto.getBeanNamesForType(DataSeeder.class)).isEmpty();
        assertThat(mongo.getCollection("usuarios").countDocuments()).isZero();
    }
}
