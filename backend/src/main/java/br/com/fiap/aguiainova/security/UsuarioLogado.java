package br.com.fiap.aguiainova.security;

import br.com.fiap.aguiainova.model.Autor;
import br.com.fiap.aguiainova.model.enums.Perfil;

/** Usuario autenticado da requisicao atual. Injete nos controllers com {@code @AuthenticationPrincipal}. */
public record UsuarioLogado(String id, String nome, String email, Perfil perfil) {

    public boolean eOperador() {
        return perfil == Perfil.OPERADOR;
    }

    public Autor comoAutor() {
        return new Autor(id, nome);
    }
}
