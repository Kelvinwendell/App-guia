package br.com.fiap.aguiainova.integration.ai;

import java.util.List;

/**
 * Pedido de geracao de texto, independente do provedor.
 *
 * @param instrucaoSistema regras e papel do modelo
 * @param mensagens        conversa, sempre comecando por uma mensagem do usuario e alternando os papeis
 * @param respostaJson     pede ao provedor (quando suportado) que responda somente com JSON
 */
public record PedidoLlm(String instrucaoSistema, List<MensagemLlm> mensagens, boolean respostaJson) {
}
