package br.com.fiap.aguiainova.security;

import br.com.fiap.aguiainova.model.Usuario;
import br.com.fiap.aguiainova.repository.UsuarioRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Transforma o JWT ja validado (assinatura, expiracao, emissor) em uma autenticacao. O perfil e recarregado do
 * banco a cada requisicao: se um usuario for removido, desativado ou rebaixado, o efeito e imediato e nao
 * depende de o token expirar.
 */
@Component
public class UsuarioJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UsuarioRepository usuarios;

    public UsuarioJwtConverter(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Usuario usuario = usuarios.findById(jwt.getSubject())
                .filter(Usuario::isAtivo)
                .orElseThrow(() -> new BadCredentialsException("Usuário do token inexistente ou inativo"));
        return new UsuarioAuthenticationToken(jwt,
                new UsuarioLogado(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil()));
    }
}
