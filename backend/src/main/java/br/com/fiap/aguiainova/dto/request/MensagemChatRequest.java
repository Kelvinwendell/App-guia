package br.com.fiap.aguiainova.dto.request;

import br.com.fiap.aguiainova.model.enums.PapelMensagem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MensagemChatRequest(
        @NotNull PapelMensagem papel,
        @NotBlank @Size(max = 2000) String texto) {
}
