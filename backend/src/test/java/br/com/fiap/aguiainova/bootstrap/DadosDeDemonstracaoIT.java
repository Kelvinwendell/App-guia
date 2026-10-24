package br.com.fiap.aguiainova.bootstrap;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import br.com.fiap.aguiainova.TestcontainersConfiguration;
import br.com.fiap.aguiainova.bootstrap.RoteiroDemo.EstrategiaDemo;
import br.com.fiap.aguiainova.dto.request.EstrategiaRequest;
import br.com.fiap.aguiainova.dto.request.IdeiaRequest;
import br.com.fiap.aguiainova.model.DadosUsuario;
import br.com.fiap.aguiainova.model.Usuario;
import br.com.fiap.aguiainova.model.enums.Perfil;
import br.com.fiap.aguiainova.model.enums.StatusEstrategia;
import br.com.fiap.aguiainova.repository.UsuarioRepository;
import br.com.fiap.aguiainova.security.UsuarioLogado;
import br.com.fiap.aguiainova.service.EstrategiaService;
import br.com.fiap.aguiainova.service.IdeiaService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sobe a aplicacao com a carga de demonstracao ligada, como no Render, e confere o que o app vai encontrar. Os testes
 * compartilham o banco carregado na subida, por isso rodam em ordem e o que apaga dados e o ultimo.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(properties = {"app.seed.enabled=false", "app.seed.demo.modo=carregar",
        "app.seed.demo.senha=" + DadosDeDemonstracaoIT.SENHA})
class DadosDeDemonstracaoIT {

    static final String SENHA = "demonstracao-123";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private MongoTemplate mongo;
    @Autowired
    private JsonMapper json;
    @Autowired
    private DadosDeDemonstracao demo;
    @Autowired
    private UsuarioRepository usuarios;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private EstrategiaService estrategias;
    @Autowired
    private IdeiaService ideias;

    private ResultActions entrar(String email, String senha) throws Exception {
        return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("email", email, "senha", senha))));
    }

    private String token(String email) throws Exception {
        String corpo = entrar(email, SENHA).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(corpo, "$.accessToken");
    }

    private ResultActions como(String chaveDaConta, String url) throws Exception {
        return mvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token(emailDe(chaveDaConta))));
    }

    private static String emailDe(String chave) {
        return RoteiroDemo.usuarios().stream().filter(u -> u.chave().equals(chave)).findFirst().orElseThrow().email();
    }

    private static String tituloDaEstrategia(String chave) {
        return RoteiroDemo.estrategias().stream().filter(e -> e.chave().equals(chave)).map(EstrategiaDemo::titulo)
                .findFirst().orElseThrow();
    }

    private long quantos(String colecao) {
        return mongo.getCollection(colecao).countDocuments();
    }

    @Test
    @Order(1)
    void asCincoContasEntramComSeuPerfil() throws Exception {
        for (RoteiroDemo.UsuarioDemo conta : RoteiroDemo.usuarios()) {
            como(conta.chave(), "/api/auth/me")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(conta.email()))
                    .andExpect(jsonPath("$.perfil").value(conta.perfil().name()));
        }
    }

    @Test
    @Order(2)
    void criaTodoORoteiroPelasRegrasDaApi() throws Exception {
        assertThat(quantos("usuarios")).isEqualTo(RoteiroDemo.usuarios().size());
        assertThat(quantos("estrategias")).isEqualTo(RoteiroDemo.estrategias().size());
        assertThat(quantos("ideias")).isEqualTo(RoteiroDemo.ideias().size());
        assertThat(quantos("projetos")).isEqualTo(RoteiroDemo.projetos().size());

        como("lideranca", "/api/estrategias").andExpect(jsonPath("$.length()").value(6));
        // uma entrada por estrategia criada mais a revisao da descricao da transformacao digital
        como("lideranca", "/api/estrategias/historico").andExpect(jsonPath("$.length()").value(7));
        como("gestor", "/api/ideias").andExpect(jsonPath("$.length()").value(12));
        como("gestor", "/api/projetos").andExpect(jsonPath("$.length()").value(5));

        como("lideranca", "/api/dashboard/resumo")
                .andExpect(jsonPath("$.totalProjetos").value(5))
                .andExpect(jsonPath("$.projetosAtrasados").value(1))
                .andExpect(jsonPath("$.roiPercentual").value(greaterThan(0.0)))
                .andExpect(jsonPath("$.ideias.aprovadas").value(7))
                .andExpect(jsonPath("$.ideias.rejeitadas").value(1))
                .andExpect(jsonPath("$.ideias.emAnalise").value(4));
        como("lideranca", "/api/dashboard/evolucao").andExpect(jsonPath("$.length()").value(greaterThan(3)));
    }

    @Test
    @Order(3)
    void asEstrategiasVigentesMostramAsTresSaudes() throws Exception {
        String corpo = como("lideranca", "/api/estrategias/saude").andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<Map<String, Object>> itens = JsonPath.read(corpo, "$");

        Map<String, Object> niveis = itens.stream()
                .collect(Collectors.toMap(item -> (String) item.get("titulo"), item -> item.get("nivel")));

        assertThat(niveis).containsOnly(
                entry(tituloDaEstrategia("desperdicio"), "EM_DIA"),
                entry(tituloDaEstrategia("digital"), "EM_DIA"),
                entry(tituloDaEstrategia("seguranca"), "EM_RISCO"),
                entry(tituloDaEstrategia("energia"), "EM_RISCO"),
                entry(tituloDaEstrategia("qualidade"), "SEM_TRACAO"));
    }

    @Test
    @Order(4)
    void carregarDeNovoNaoDuplicaNadaNemTrocaASenha() throws Exception {
        demo.carregar("outra-senha-999");

        assertThat(quantos("usuarios")).isEqualTo(RoteiroDemo.usuarios().size());
        assertThat(quantos("estrategias")).isEqualTo(RoteiroDemo.estrategias().size());
        assertThat(quantos("ideias")).isEqualTo(RoteiroDemo.ideias().size());
        assertThat(quantos("projetos")).isEqualTo(RoteiroDemo.projetos().size());
        entrar(RoteiroDemo.EMAIL_DA_LIDERANCA, SENHA).andExpect(status().isOk());
        entrar(RoteiroDemo.EMAIL_DA_LIDERANCA, "outra-senha-999").andExpect(status().isUnauthorized());
    }

    @Test
    @Order(5)
    void removerApagaSoOQueAsContasDeDemonstracaoCriaram() throws Exception {
        Usuario real = usuarios.save(new Usuario(new DadosUsuario("Pessoa Real", "pessoa.real@empresa.com",
                encoder.encode("senha-real-123"), Perfil.GESTOR, "Cargo", "Departamento", "Unidade")));
        UsuarioLogado logado = new UsuarioLogado(real.getId(), real.getNome(), real.getEmail(), real.getPerfil());
        estrategias.criar(new EstrategiaRequest("Estratégia real", "Descrição da estratégia real.", "Geral",
                "Campanha real", StatusEstrategia.VIGENTE), logado);
        ideias.criar(new IdeiaRequest("Ideia real", "Descrição da ideia real.", "Geral", null), logado);

        demo.remover();

        assertThat(quantos("usuarios")).isEqualTo(1);
        assertThat(quantos("estrategias")).isEqualTo(1);
        assertThat(quantos("ideias")).isEqualTo(1);
        assertThat(quantos("projetos")).isZero();
        assertThat(quantos("estrategias_historico")).isEqualTo(1);
        entrar(RoteiroDemo.EMAIL_DA_LIDERANCA, SENHA).andExpect(status().isUnauthorized());
        entrar("pessoa.real@empresa.com", "senha-real-123").andExpect(status().isOk());

        demo.remover();
        assertThat(quantos("usuarios")).isEqualTo(1);

        demo.carregar(SENHA);
        assertThat(quantos("usuarios")).isEqualTo(1 + RoteiroDemo.usuarios().size());
        assertThat(quantos("projetos")).isEqualTo(RoteiroDemo.projetos().size());
        entrar(RoteiroDemo.EMAIL_DA_LIDERANCA, SENHA).andExpect(status().isOk());
    }
}
