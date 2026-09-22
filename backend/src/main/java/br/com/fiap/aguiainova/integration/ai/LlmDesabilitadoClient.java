package br.com.fiap.aguiainova.integration.ai;

/** Implementacao nula usada quando a IA esta desabilitada ou sem chave: a aplicacao segue funcionando sem ela. */
public class LlmDesabilitadoClient implements LlmClient {

    @Override
    public String provedor() {
        return "NENHUM";
    }

    @Override
    public String modelo() {
        return "";
    }

    @Override
    public boolean disponivel() {
        return false;
    }

    @Override
    public String gerar(PedidoLlm pedido) {
        throw new LlmNaoConfiguradoException();
    }
}
