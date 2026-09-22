package br.com.fiap.aguiainova.dto.response;

import br.com.fiap.aguiainova.model.enums.Perfil;

public record UsuarioResponse(
        String id,
        String nome,
        String email,
        Perfil perfil,
        String cargo,
        String departamento,
        String unidade,
        boolean ativo) {
}
