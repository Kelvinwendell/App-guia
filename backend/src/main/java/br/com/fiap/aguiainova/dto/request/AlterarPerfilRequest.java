package br.com.fiap.aguiainova.dto.request;

import br.com.fiap.aguiainova.model.enums.Perfil;
import jakarta.validation.constraints.NotNull;

public record AlterarPerfilRequest(@NotNull Perfil perfil) {
}
