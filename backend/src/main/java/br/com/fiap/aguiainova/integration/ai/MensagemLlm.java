package br.com.fiap.aguiainova.integration.ai;

import br.com.fiap.aguiainova.model.enums.PapelMensagem;

public record MensagemLlm(PapelMensagem papel, String texto) {
}
