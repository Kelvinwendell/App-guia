package br.com.fiap.aguiainova.model;

import java.util.List;

import br.com.fiap.aguiainova.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexInfo;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Campos usados com frequencia em filtro/ordenacao (status, etapa) precisam de indice: sem
 * isso, listagens e o dashboard fazem varredura completa da colecao conforme o volume cresce.
 * spring.data.mongodb.auto-index-creation=true (application.yml) cria os indices no boot a
 * partir das anotacoes do modelo, entao basta confirmar que o indice existe de fato no banco.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class IndicesDeConsultaIT {

    @Autowired
    private MongoTemplate mongo;

    private boolean existeIndiceCobrindoCampo(String colecao, String campo) {
        List<IndexInfo> indices = mongo.indexOps(colecao).getIndexInfo();
        return indices.stream().anyMatch(indice -> indice.getIndexFields().stream()
                .anyMatch(campoIndexado -> campoIndexado.getKey().equals(campo)));
    }

    @Test
    void ideiasTemIndiceEmStatus() {
        assertThat(existeIndiceCobrindoCampo("ideias", "status")).isTrue();
    }

    @Test
    void projetosTemIndiceEmStatus() {
        assertThat(existeIndiceCobrindoCampo("projetos", "status")).isTrue();
    }

    @Test
    void projetosTemIndiceEmEtapa() {
        assertThat(existeIndiceCobrindoCampo("projetos", "etapa")).isTrue();
    }

    @Test
    void estrategiasTemIndiceEmStatus() {
        assertThat(existeIndiceCobrindoCampo("estrategias", "status")).isTrue();
    }
}
