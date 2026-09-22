package br.com.fiap.aguiainova.config;

import br.com.fiap.aguiainova.integration.ai.GeminiLlmClient;
import br.com.fiap.aguiainova.integration.ai.LlmClient;
import br.com.fiap.aguiainova.integration.ai.LlmDesabilitadoClient;
import br.com.fiap.aguiainova.integration.ai.LlmNaoConfiguradoException;
import br.com.fiap.aguiainova.integration.ai.OpenAiCompatibleLlmClient;
import br.com.fiap.aguiainova.support.Dados;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IaConfigTest {

    private final IaConfig config = new IaConfig();

    private LlmClient criar(boolean habilitada, String chave, String provedor) {
        return config.llmClient(Dados.propriedades(true, habilitada, chave, provedor));
    }

    @Test
    void semChaveDeApiAIaFicaDesabilitadaSemQuebrarAAplicacao() {
        LlmClient cliente = criar(true, "", "gemini");

        assertThat(cliente).isInstanceOf(LlmDesabilitadoClient.class);
        assertThat(cliente.disponivel()).isFalse();
        assertThat(cliente.provedor()).isEqualTo("NENHUM");
        assertThatThrownBy(() -> cliente.gerar(null)).isInstanceOf(LlmNaoConfiguradoException.class);
    }

    @Test
    void iaDesligadaIgnoraAChave() {
        assertThat(criar(false, "chave", "gemini")).isInstanceOf(LlmDesabilitadoClient.class);
    }

    @Test
    void escolheOProvedorPelaConfiguracao() {
        assertThat(criar(true, "chave", "gemini")).isInstanceOf(GeminiLlmClient.class);
        assertThat(criar(true, "chave", " GEMINI ")).isInstanceOf(GeminiLlmClient.class);
        assertThat(criar(true, "chave", "openai")).isInstanceOf(OpenAiCompatibleLlmClient.class);
    }

    @Test
    void provedorDesconhecidoFalhaNaInicializacaoMesmoSemChave() {
        assertThatThrownBy(() -> criar(true, "", "claude-do-vizinho"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("app.ai.provider inválido");
    }
}
