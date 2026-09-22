package br.com.fiap.aguiainova.dto.request;

import br.com.fiap.aguiainova.model.enums.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Email @Size(max = 160) String email,
        // BCrypt ignora o que passa de 72 bytes; limitar evita a falsa sensacao de senha longa.
        @NotBlank @Size(min = 6, max = 72) String senha,
        /** Opcional. Só é respeitado quando app.security.allow-role-selection=true; senão vira OPERADOR. */
        Perfil perfil,
        @Size(max = 80) String cargo,
        @Size(max = 80) String departamento,
        @Size(max = 80) String unidade) {

    /** Teclados de celular costumam acrescentar um espaco ao autocompletar o e-mail; normalizamos antes de validar. */
    public RegistroRequest {
        email = email == null ? null : email.strip();
    }
}
