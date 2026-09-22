package br.com.fiap.aguiainova.bootstrap;

import java.util.Map;

import br.com.fiap.aguiainova.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Sobe a aplicacao com o seed ligado, exatamente como no primeiro uso, e confere o que o app vai encontrar. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = "app.seed.enabled=true")
class DataSeederIT {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private MongoTemplate mongo;
    @Autowired
    private JsonMapper json;
    @Autowired
    private DataSeeder seeder;

    private String login(String email) throws Exception {
        String corpo = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", email, "senha", "123456"))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(corpo, "$.accessToken");
    }

    private ResultActions comoUsuario(String email, String url) throws Exception {
        return mvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + login(email)));
    }

    @Test
    void osTresUsuariosDeTesteDaDocumentacaoConseguemEntrarComSeuPerfil() throws Exception {
        for (Map.Entry<String, String> conta : Map.of("vinicius@gmail.com", "OPERADOR", "anna@gmail.com", "GESTOR",
                "gabriela@gmail.com", "LIDERANCA").entrySet()) {
            comoUsuario(conta.getKey(), "/api/auth/me")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.perfil").value(conta.getValue()));
        }
    }

    @Test
    void criaDadosDeExemploCoerentesParaOsTresPerfis() throws Exception {
        comoUsuario("gabriela@gmail.com", "/api/estrategias").andExpect(jsonPath("$.length()").value(4));
        comoUsuario("gabriela@gmail.com", "/api/estrategias/historico").andExpect(jsonPath("$.length()").value(5));
        comoUsuario("anna@gmail.com", "/api/ideias").andExpect(jsonPath("$.length()").value(6));
        comoUsuario("vinicius@gmail.com", "/api/ideias").andExpect(jsonPath("$.length()").value(6));
        comoUsuario("anna@gmail.com", "/api/projetos").andExpect(jsonPath("$.length()").value(4));

        comoUsuario("gabriela@gmail.com", "/api/dashboard/resumo")
                .andExpect(jsonPath("$.totalProjetos").value(4))
                .andExpect(jsonPath("$.projetosAtrasados").value(1))
                .andExpect(jsonPath("$.roiPercentual").value(greaterThan(0.0)))
                .andExpect(jsonPath("$.ideias.aprovadas").value(3))
                .andExpect(jsonPath("$.ideias.rejeitadas").value(1))
                .andExpect(jsonPath("$.ideias.emAnalise").value(2));
        comoUsuario("gabriela@gmail.com", "/api/dashboard/evolucao").andExpect(jsonPath("$.length()").value(greaterThan(3)));
    }

    @Test
    void executarOSeedDeNovoNaoDuplicaNada() {
        long usuarios = mongo.getCollection("usuarios").countDocuments();
        long projetos = mongo.getCollection("projetos").countDocuments();

        seeder.run(new DefaultApplicationArguments());

        assertThat(mongo.getCollection("usuarios").countDocuments()).isEqualTo(usuarios).isEqualTo(3);
        assertThat(mongo.getCollection("projetos").countDocuments()).isEqualTo(projetos).isEqualTo(4);
    }
}
