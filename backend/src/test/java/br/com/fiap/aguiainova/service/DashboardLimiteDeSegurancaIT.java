package br.com.fiap.aguiainova.service;

import br.com.fiap.aguiainova.model.Usuario;
import br.com.fiap.aguiainova.model.enums.Perfil;
import br.com.fiap.aguiainova.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * DashboardService agregava estrategias/projetos/ideias com findAll() sem limite nenhum
 * (carregava a colecao inteira na memoria a cada requisicao). Limite baixo aqui so para nao
 * precisar criar milhares de documentos; o valor de producao e configurado em application.yml.
 */
@TestPropertySource(properties = "app.dashboard.limite-registros-agregacao=5")
class DashboardLimiteDeSegurancaIT extends AbstractIntegrationTest {

    @Test
    void porEstrategiaNuncaRetornaMaisLinhasDoQueOLimiteConfigurado() throws Exception {
        Usuario lideranca = criarUsuario(Perfil.LIDERANCA);
        for (int i = 0; i < 8; i++) {
            cenarios.estrategia(logado(lideranca), "Estratégia " + i, "Geral", null);
        }

        get(lideranca, "/api/dashboard/estrategias")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.lessThanOrEqualTo(5)));
    }
}
