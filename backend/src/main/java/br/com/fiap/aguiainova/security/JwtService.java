package br.com.fiap.aguiainova.security;

import java.time.Duration;
import java.time.Instant;

import br.com.fiap.aguiainova.config.AppProperties;
import br.com.fiap.aguiainova.model.Usuario;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    public record TokenEmitido(String valor, Instant expiraEm, long expiraEmSegundos) {
    }

    private final JwtEncoder encoder;
    private final AppProperties.Jwt config;

    public JwtService(JwtEncoder encoder, AppProperties props) {
        this.encoder = encoder;
        this.config = props.jwt();
    }

    public TokenEmitido emitir(Usuario usuario) {
        Instant agora = Instant.now();
        Duration validade = Duration.ofMinutes(config.expirationMinutes());
        Instant expiracao = agora.plus(validade);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(config.issuer())
                .subject(usuario.getId())
                .issuedAt(agora)
                .expiresAt(expiracao)
                .claim("nome", usuario.getNome())
                .claim("email", usuario.getEmail())
                .claim("perfil", usuario.getPerfil().name())
                .build();
        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(cabecalho, claims)).getTokenValue();
        return new TokenEmitido(token, expiracao, validade.toSeconds());
    }
}
