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

    @Test
    void rascunhoCriticaELeituraDeEstrategiaDividemOLimiteDeIaDoUsuario() throws Exception {
        Usuario lideranca = criarUsuario(Perfil.LIDERANCA);
        String estrategia = cenarios.estrategia(logado(lideranca), "Estratégia de teste", "Geral", "C1");
        Map<String, Object> rascunho = Map.of("objetivo", "Reduzir o desperdício");
        Map<String, Object> analise = Map.of("estrategia", Map.of("titulo", "Título da estratégia",
                "descricao", "Descrição", "categoria", "Geral"));
        String leitura = "/api/ia/estrategias/" + estrategia + "/saude";

        // tres chamadas passam (rascunho sem IA: 503; critica e leitura caem nas regras: 200) e esgotam o balde
        post(lideranca, "/api/ia/estrategias/rascunho", rascunho).andExpect(status().isServiceUnavailable());
        post(lideranca, "/api/ia/estrategias/analise", analise).andExpect(status().isOk());
        post(lideranca, leitura, null).andExpect(status().isOk());

        post(lideranca, "/api/ia/estrategias/rascunho", rascunho).andExpect(status().isTooManyRequests());
        post(lideranca, "/api/ia/estrategias/analise", analise).andExpect(status().isTooManyRequests());
        post(lideranca, leitura, null).andExpect(status().isTooManyRequests());
    }

    @Test
    void leituraDaSaudeDeEstrategiaDividemOLimiteComOChatDoGestor() throws Exception {
        Usuario gestor = criarUsuario(Perfil.GESTOR);
        String estrategia = cenarios.estrategia(logado(criarUsuario(Perfil.LIDERANCA)), "Estratégia de teste", "Geral", "C1");
        Map<String, Object> chat = Map.of("mensagem", "quais ideias priorizar?");

        post(gestor, "/api/ia/chat", chat).andExpect(status().isServiceUnavailable());
        post(gestor, "/api/ia/chat", chat).andExpect(status().isServiceUnavailable());
        post(gestor, "/api/ia/estrategias/" + estrategia + "/saude", null).andExpect(status().isOk());

        post(gestor, "/api/ia/chat", chat).andExpect(status().isTooManyRequests());
        post(gestor, "/api/ia/estrategias/" + estrategia + "/saude", null).andExpect(status().isTooManyRequests());
    }

    @Test
    void oLimiteDeIaDasEstrategiasEPorUsuario() throws Exception {
        Usuario esgotado = criarUsuario(Perfil.LIDERANCA);
        Usuario outro = criarUsuario(Perfil.LIDERANCA);
        Map<String, Object> analise = Map.of("estrategia", Map.of("titulo", "Título da estratégia",
                "descricao", "Descrição", "categoria", "Geral"));
        for (int i = 0; i < 3; i++) {
            post(esgotado, "/api/ia/estrategias/analise", analise).andExpect(status().isOk());
        }
        post(esgotado, "/api/ia/estrategias/analise", analise).andExpect(status().isTooManyRequests());

        post(outro, "/api/ia/estrategias/analise", analise).andExpect(status().isOk());
    }
}
