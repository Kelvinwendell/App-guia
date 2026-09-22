package br.com.fiap.aguiainova.controller;

import java.util.Map;

import br.com.fiap.aguiainova.model.Usuario;
import br.com.fiap.aguiainova.model.enums.Perfil;
import br.com.fiap.aguiainova.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerIT extends AbstractIntegrationTest {

    private Map<String, Object> cadastro(String email) {
        return Map.of("nome", "Maria Silva", "email", email, "senha", "segredo1", "perfil", "GESTOR",
                "cargo", "Analista");
    }

    @Test
    void cadastraUsuarioNormalizandoEmailEGuardandoSenhaComHash() throws Exception {
        postSemToken("/api/auth/register", cadastro("  Maria@Teste.COM "))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("maria@teste.com"))
                .andExpect(jsonPath("$.perfil").value("GESTOR"))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.senhaHash").doesNotExist());

        Usuario salvo = usuarios.findByEmail("maria@teste.com").orElseThrow();
        assertThat(salvo.getSenhaHash()).startsWith("$2").isNotEqualTo("segredo1");
    }

    @Test
    void rejeitaEmailJaCadastrado() throws Exception {
        postSemToken("/api/auth/register", cadastro("maria@teste.com")).andExpect(status().isCreated());

        postSemToken("/api/auth/register", cadastro("MARIA@teste.com"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Já existe um usuário cadastrado com este e-mail."));
    }

    @Test
    void rejeitaDadosInvalidosDetalhandoCadaCampo() throws Exception {
        postSemToken("/api/auth/register", Map.of("nome", "", "email", "nao-e-email", "senha", "123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.nome").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.senha").exists());
    }

    @Test
    void loginDevolveTokenEDadosDoUsuario() throws Exception {
        Usuario gestor = criarUsuario(Perfil.GESTOR);

        postSemToken("/api/auth/login", Map.of("email", gestor.getEmail().toUpperCase(), "senha", SENHA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").isNumber())
                .andExpect(jsonPath("$.usuario.id").value(gestor.getId()))
                .andExpect(jsonPath("$.usuario.perfil").value("GESTOR"));
    }

    @Test
    void senhaErradaEEmailInexistenteRecebemExatamenteAMesmaResposta() throws Exception {
        Usuario gestor = criarUsuario(Perfil.GESTOR);

        String senhaErrada = postSemToken("/api/auth/login", Map.of("email", gestor.getEmail(), "senha", "errada!"))
                .andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
        String emailInexistente = postSemToken("/api/auth/login", Map.of("email", "ninguem@teste.com", "senha", SENHA))
                .andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();

        assertThat(senhaErrada).isEqualTo(emailInexistente).contains("E-mail ou senha inválidos.");
    }

    @Test
    void meDevolveOUsuarioDoToken() throws Exception {
        Usuario operador = criarUsuario(Perfil.OPERADOR);

        get(operador, "/api/auth/me")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(operador.getId()))
                .andExpect(jsonPath("$.email").value(operador.getEmail()));
    }

    @Test
    void semTokenAPIResponde401EmFormatoProblemDetail() throws Exception {
        getSemToken("/api/ideias")
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    @Test
    void tokenAdulteradoOuInventadoEhRecusado() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized());
        Usuario operador = criarUsuario(Perfil.OPERADOR);
        String token = jwt.emitir(operador).valor();
        mvc.perform(MockMvcRequestBuilders.get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token + "x"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenDeUsuarioRemovidoDeixaDeFuncionar() throws Exception {
        Usuario operador = criarUsuario(Perfil.OPERADOR);
        get(operador, "/api/auth/me").andExpect(status().isOk());

        usuarios.deleteById(operador.getId());

        get(operador, "/api/auth/me").andExpect(status().isUnauthorized());
    }

    @Test
    void perfilVemDoBancoEntaoRebaixarUmUsuarioTemEfeitoImediato() throws Exception {
        Usuario lideranca = criarUsuario(Perfil.LIDERANCA);
        get(lideranca, "/api/usuarios").andExpect(status().isOk());

        lideranca.alterarPerfil(Perfil.OPERADOR);
        usuarios.save(lideranca);

        get(lideranca, "/api/usuarios").andExpect(status().isForbidden());
    }

    @Test
    void saudeEDocumentacaoSaoPublicas() throws Exception {
        getSemToken("/actuator/health").andExpect(status().isOk());
        getSemToken("/v3/api-docs").andExpect(status().isOk()).andExpect(jsonPath("$.info.title").value("Águia Inova API"));
    }
}
