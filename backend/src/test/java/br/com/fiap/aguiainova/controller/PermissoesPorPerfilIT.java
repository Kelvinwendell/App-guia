package br.com.fiap.aguiainova.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import br.com.fiap.aguiainova.model.Usuario;
import br.com.fiap.aguiainova.model.enums.Perfil;
import br.com.fiap.aguiainova.model.enums.StatusProjeto;
import br.com.fiap.aguiainova.security.UsuarioLogado;
import br.com.fiap.aguiainova.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Matriz completa de permissoes: para cada endpoint da API, o status HTTP esperado para cada perfil. Um 403 aqui
 * significa "perfil sem acesso"; qualquer outro status significa que a requisicao foi aceita e processada.
 */
class PermissoesPorPerfilIT extends AbstractIntegrationTest {

    private static final String PRAZO_LONGE = LocalDate.now().plusYears(5).toString();
    private static final Map<String, Object> ESTRATEGIA = Map.of("titulo", "Nova estratégia", "descricao", "Descrição",
            "categoria", "Tecnologia", "campanha", "Campanha X");
    private static final Map<String, Object> IDEIA = Map.of("titulo", "Ideia nova", "descricao", "Descrição da ideia",
            "categoria", "Tecnologia");
    private static final Map<String, Object> PROJETO = Map.of("nome", "Projeto novo", "descricao", "Descrição",
            "investimento", 1000, "prazo", PRAZO_LONGE);
    private static final Map<String, Object> RESULTADO = Map.of("descricao", "Ganho", "retornoFinanceiro", 10);

    private record Endpoint(HttpMethod metodo, String caminho, Object corpo, int operador, int gestor, int lideranca) {

        int esperadoPara(Perfil perfil) {
            return switch (perfil) {
                case OPERADOR -> operador;
                case GESTOR -> gestor;
                case LIDERANCA -> lideranca;
            };
        }

        @Override
        public String toString() {
            return metodo + " " + caminho;
        }
    }

    private static Endpoint ep(HttpMethod metodo, String caminho, Object corpo, int operador, int gestor,
            int lideranca) {
        return new Endpoint(metodo, caminho, corpo, operador, gestor, lideranca);
    }

    static Stream<Arguments> casos() {
        List<Endpoint> endpoints = List.of(
                ep(HttpMethod.GET, "/api/auth/me", null, 200, 200, 200),
                ep(HttpMethod.GET, "/api/usuarios", null, 403, 403, 200),
                ep(HttpMethod.PATCH, "/api/usuarios/{alvo}/perfil", Map.of("perfil", "GESTOR"), 403, 403, 200),
                ep(HttpMethod.GET, "/api/estrategias", null, 200, 200, 200),
                ep(HttpMethod.GET, "/api/estrategias/{estrategia}", null, 200, 200, 200),
                ep(HttpMethod.GET, "/api/estrategias/historico", null, 200, 200, 200),
                ep(HttpMethod.GET, "/api/estrategias/{estrategia}/historico", null, 200, 200, 200),
                ep(HttpMethod.POST, "/api/estrategias", ESTRATEGIA, 403, 403, 201),
                ep(HttpMethod.PUT, "/api/estrategias/{estrategia}", ESTRATEGIA, 403, 403, 200),
                ep(HttpMethod.DELETE, "/api/estrategias/{estrategia}", null, 403, 403, 204),
                ep(HttpMethod.GET, "/api/ideias", null, 200, 200, 403),
                ep(HttpMethod.GET, "/api/ideias/aprovadas", null, 200, 200, 200),
                ep(HttpMethod.GET, "/api/ideias/{ideia}", null, 200, 200, 403),
                ep(HttpMethod.POST, "/api/ideias", IDEIA, 201, 403, 403),
                ep(HttpMethod.PUT, "/api/ideias/{ideia}", IDEIA, 200, 403, 403),
                ep(HttpMethod.DELETE, "/api/ideias/{ideia}", null, 204, 403, 403),
                ep(HttpMethod.PATCH, "/api/ideias/{ideia}/avaliacao", Map.of("status", "APROVADA"), 403, 200, 403),
                ep(HttpMethod.GET, "/api/projetos", null, 403, 200, 200),
                ep(HttpMethod.GET, "/api/projetos/{projeto}", null, 403, 200, 200),
                ep(HttpMethod.POST, "/api/projetos", PROJETO, 403, 201, 403),
                ep(HttpMethod.PUT, "/api/projetos/{projeto}", PROJETO, 403, 200, 403),
                ep(HttpMethod.PATCH, "/api/projetos/{projeto}/progresso", Map.of("progresso", 10), 403, 200, 403),
                ep(HttpMethod.POST, "/api/projetos/{projeto}/resultados", RESULTADO, 403, 201, 403),
                ep(HttpMethod.DELETE, "/api/projetos/{projeto}", null, 403, 204, 403),
                ep(HttpMethod.GET, "/api/dashboard/resumo", null, 403, 403, 200),
                ep(HttpMethod.GET, "/api/dashboard/estrategias", null, 403, 403, 200),
                ep(HttpMethod.GET, "/api/dashboard/projetos", null, 403, 403, 200),
                ep(HttpMethod.GET, "/api/dashboard/evolucao", null, 403, 403, 200),
                ep(HttpMethod.GET, "/api/ia/status", null, 200, 200, 200),
                ep(HttpMethod.POST, "/api/ia/ideias/{ideia}/avaliar", null, 403, 200, 403),
                ep(HttpMethod.POST, "/api/ia/ideias/avaliar-pendentes", null, 403, 200, 403),
                // sem chave de API configurada nos testes, o chat responde 503 (mas so o gestor chega ate la)
                ep(HttpMethod.POST, "/api/ia/chat", Map.of("mensagem", "Quais ideias priorizar?"), 403, 503, 403),
                ep(HttpMethod.POST, "/api/ia/dashboard/insights", null, 403, 403, 200));
        return endpoints.stream().flatMap(endpoint -> Stream.of(Perfil.values())
                .map(perfil -> Arguments.of(endpoint, perfil)));
    }

    @DisplayName("cada perfil so acessa o que o seu nivel permite")
    @ParameterizedTest(name = "{0} como {1}")
    @MethodSource("casos")
    void respeitaOsNiveisDeAcesso(Endpoint endpoint, Perfil perfil) throws Exception {
        Usuario chamador = criarUsuario(perfil);
        Usuario donoDaIdeia = perfil == Perfil.OPERADOR ? chamador : criarUsuario(Perfil.OPERADOR);
        UsuarioLogado lideranca = logado(perfil == Perfil.LIDERANCA ? chamador : criarUsuario(Perfil.LIDERANCA));
        UsuarioLogado gestor = logado(perfil == Perfil.GESTOR ? chamador : criarUsuario(Perfil.GESTOR));

        String estrategia = cenarios.estrategia(lideranca, "Estratégia base", "Geral", "Campanha");
        String ideia = cenarios.ideia(logado(donoDaIdeia), "Ideia base", null);
        String projeto = cenarios.projeto(gestor, "Projeto base", "1000", LocalDate.now().plusYears(5),
                StatusProjeto.EM_ANDAMENTO, null);
        String alvo = criarUsuario(Perfil.OPERADOR).getId();

        String caminho = endpoint.caminho().replace("{estrategia}", estrategia).replace("{ideia}", ideia)
                .replace("{projeto}", projeto).replace("{alvo}", alvo);

        executar(chamador, endpoint, caminho).andExpect(status().is(endpoint.esperadoPara(perfil)));
    }

    private ResultActions executar(Usuario chamador, Endpoint endpoint, String caminho) throws Exception {
        MockHttpServletRequestBuilder requisicao = MockMvcRequestBuilders.request(endpoint.metodo(), caminho);
        if (endpoint.corpo() != null) {
            requisicao = comCorpo(requisicao, endpoint.corpo());
        }
        return mvc.perform(autenticado(requisicao, chamador));
    }
}
