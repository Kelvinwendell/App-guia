package br.com.fiap.aguiainova.support;

import java.util.UUID;

import br.com.fiap.aguiainova.TestcontainersConfiguration;
import br.com.fiap.aguiainova.model.DadosUsuario;
import br.com.fiap.aguiainova.model.Usuario;
import br.com.fiap.aguiainova.model.enums.Perfil;
import br.com.fiap.aguiainova.repository.UsuarioRepository;
import br.com.fiap.aguiainova.security.JwtService;
import br.com.fiap.aguiainova.security.UsuarioLogado;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.json.JsonMapper;

/**
 * Base dos testes de integracao: contexto completo da aplicacao contra um MongoDB real em container. O banco e
 * esvaziado antes de cada teste (os indices sao mantidos) e ha helpers para autenticar como cada perfil.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, Cenarios.class})
@TestPropertySource(properties = "app.seed.enabled=false")
public abstract class AbstractIntegrationTest {

    protected static final String SENHA = "123456";

    private static String senhaHashPadrao;

    @Autowired
    protected MockMvc mvc;
    @Autowired
    protected JsonMapper json;
    @Autowired
    protected MongoTemplate mongo;
    @Autowired
    protected UsuarioRepository usuarios;
    @Autowired
    protected PasswordEncoder encoder;
    @Autowired
    protected JwtService jwt;
    @Autowired
    protected Cenarios cenarios;

    @BeforeEach
    void limparBanco() {
        mongo.getCollectionNames().forEach(colecao -> mongo.remove(new Query(), colecao));
    }

    /** BCrypt e propositalmente lento; o hash da senha padrao e calculado uma vez e reaproveitado. */
    private String senhaHash() {
        if (senhaHashPadrao == null) {
            senhaHashPadrao = encoder.encode(SENHA);
        }
        return senhaHashPadrao;
    }

    protected Usuario criarUsuario(Perfil perfil) {
        String email = perfil.name().toLowerCase() + "-" + UUID.randomUUID() + "@teste.com";
        return usuarios.save(new Usuario(new DadosUsuario("Usuário " + perfil, email, senhaHash(), perfil,
                "Cargo", "Departamento", "Unidade")));
    }

    protected static UsuarioLogado logado(Usuario usuario) {
        return new UsuarioLogado(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil());
    }

    protected ResultActions getSemToken(String url) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.get(url));
    }

    protected ResultActions postSemToken(String url, Object corpo) throws Exception {
        return mvc.perform(comCorpo(MockMvcRequestBuilders.post(url), corpo));
    }

    protected ResultActions get(Usuario usuario, String url) throws Exception {
        return mvc.perform(autenticado(MockMvcRequestBuilders.get(url), usuario));
    }

    protected ResultActions delete(Usuario usuario, String url) throws Exception {
        return mvc.perform(autenticado(MockMvcRequestBuilders.delete(url), usuario));
    }

    protected ResultActions post(Usuario usuario, String url, Object corpo) throws Exception {
        return mvc.perform(comCorpo(autenticado(MockMvcRequestBuilders.post(url), usuario), corpo));
    }

    protected ResultActions put(Usuario usuario, String url, Object corpo) throws Exception {
        return mvc.perform(comCorpo(autenticado(MockMvcRequestBuilders.put(url), usuario), corpo));
    }

    protected ResultActions patch(Usuario usuario, String url, Object corpo) throws Exception {
        return mvc.perform(comCorpo(autenticado(MockMvcRequestBuilders.patch(url), usuario), corpo));
    }

    protected MockHttpServletRequestBuilder autenticado(MockHttpServletRequestBuilder requisicao, Usuario usuario) {
        return requisicao.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt.emitir(usuario).valor());
    }

    protected MockHttpServletRequestBuilder comCorpo(MockHttpServletRequestBuilder requisicao, Object corpo) {
        return requisicao.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(corpo));
    }

    /** Le um campo (JsonPath) do corpo da resposta; usado para pegar o id de um recurso recem-criado. */
    protected static String campo(ResultActions resultado, String caminho) throws Exception {
        return JsonPath.read(resultado.andReturn().getResponse().getContentAsString(), caminho);
    }
}
