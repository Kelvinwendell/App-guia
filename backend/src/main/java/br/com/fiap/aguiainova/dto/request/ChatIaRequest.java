package br.com.fiap.aguiainova.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatIaRequest(
        @NotBlank @Size(max = 2000) String mensagem,
        /** Mensagens anteriores da conversa (o servidor não guarda estado); opcional, até 20. */
        @Valid @Size(max = 20) List<MensagemChatRequest> historico) {
}
