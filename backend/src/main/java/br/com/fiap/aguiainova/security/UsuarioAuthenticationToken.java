package br.com.fiap.aguiainova.security;

import java.util.List;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/** Autenticacao resultante de um JWT valido; a autoridade ({@code ROLE_<PERFIL>}) vem do banco, nao do token. */
public class UsuarioAuthenticationToken extends AbstractAuthenticationToken {

    private final Jwt jwt;
    private final UsuarioLogado usuario;

    public UsuarioAuthenticationToken(Jwt jwt, UsuarioLogado usuario) {
        super(List.of(new SimpleGrantedAuthority("ROLE_" + usuario.perfil().name())));
        this.jwt = jwt;
        this.usuario = usuario;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return jwt.getTokenValue();
    }

    @Override
    public Object getPrincipal() {
        return usuario;
    }

    @Override
    public String getName() {
        return usuario.id();
    }
}
