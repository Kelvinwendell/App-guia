package br.com.fiap.aguiainova.security;

import java.util.Map;

import br.com.fiap.aguiainova.model.Usuario;
import br.com.fiap.aguiainova.model.enums.Perfil;
import br.com.fiap.aguiainova.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Limites pequenos nesta suite (via @TestPropertySource) so para nao precisar de dezenas de
 * chamadas por teste; o valor de producao e configurado em application.yml.
 */
@TestPropertySource(properties = {"app.rate-limit.login-por-minuto=3", "app.rate-limit.ia-por-minuto=3"})
class RateLimitingIT extends AbstractIntegrationTest {

    @Test
    void maisDeNTentativasDeLoginPorMinutoParaOMesmoEmailRecebem429() throws Exception {
        Map<String, Object> corpo = Map.of("email", "forca-bruta@teste.com", "senha", "errada");

        for (int i = 0; i < 3; i++) {
            postSemToken("/api/auth/login", corpo).andExpect(status().isUnauthorized());
        }
        postSemToken("/api/auth/login", corpo).andExpect(status().isTooManyRequests());
    }

    @Test
    void tentativasDeLoginParaEmailDiferenteNaoSofremComOLimiteDeOutroEmail() throws Exception {
        Map<String, Object> esgotado = Map.of("email", "esgotado@teste.com", "senha", "errada");
        Map<String, Object> outro = Map.of("email", "outro@teste.com", "senha", "errada");
        for (int i = 0; i < 3; i++) {
            postSemToken("/api/auth/login", esgotado).andExpect(status().isUnauthorized());
        }
        postSemToken("/api/auth/login", esgotado).andExpect(status().isTooManyRequests());

        postSemToken("/api/auth/login", outro).andExpect(status().isUnauthorized());
    }

    @Test
    void chamadasDeIaAcimaDoLimitePorUsuarioRecebem429IndependenteDoResultado() throws Exception {
        Usuario gestor = criarUsuario(Perfil.GESTOR);
        Map<String, Object> corpo = Map.of("mensagem", "quais ideias priorizar?");

        // Sem IA configurada no ambiente de teste, a chamada normal falha com 503 - o limite
        // de taxa precisa disparar de qualquer forma, independente do resultado da chamada.
        for (int i = 0; i < 3; i++) {
            post(gestor, "/api/ia/chat", corpo).andExpect(status().isServiceUnavailable());
        }
        post(gestor, "/api/ia/chat", corpo).andExpect(status().isTooManyRequests());
    }
}
