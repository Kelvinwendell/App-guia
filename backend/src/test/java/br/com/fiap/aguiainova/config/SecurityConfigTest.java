package br.com.fiap.aguiainova.config;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import br.com.fiap.aguiainova.model.DadosUsuario;
import br.com.fiap.aguiainova.model.Usuario;
import br.com.fiap.aguiainova.model.enums.Perfil;
import br.com.fiap.aguiainova.security.JwtService;
import br.com.fiap.aguiainova.support.Dados;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Regras de seguranca do JWT: assinatura, expiracao, emissor e tamanho minimo do segredo. */
class SecurityConfigTest {

    private final SecurityConfig config = new SecurityConfig();
    private final AppProperties props = Dados.propriedades(true, false, "", "gemini");
    private final SecretKey chave = config.chaveJwt(props);
    private final JwtEncoder encoder = config.jwtEncoder(chave);
    private final JwtDecoder decoder = config.jwtDecoder(chave, props);
    private final JwtService jwtService = new JwtService(encoder, props);

    private Usuario usuario() {
        return Dados.comId(new Usuario(new DadosUsuario("Ana", "ana@teste.com", "hash", Perfil.GESTOR, null, null,
                null)), "u-42");
    }

    private String token(JwtEncoder codificador, String emissor, Instant expiraEm) {
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer(emissor).subject("u-42")
                .issuedAt(expiraEm.minus(Duration.ofHours(1))).expiresAt(expiraEm).build();
        return codificador.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }

    @Test
    void tokenEmitidoCarregaOsDadosDoUsuarioEEhValido() {
        JwtService.TokenEmitido emitido = jwtService.emitir(usuario());

        Jwt jwt = decoder.decode(emitido.valor());

        assertThat(jwt.getSubject()).isEqualTo("u-42");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("aguia-inova-api");
        assertThat(jwt.<String>getClaim("perfil")).isEqualTo("GESTOR");
        assertThat(jwt.<String>getClaim("email")).isEqualTo("ana@teste.com");
        assertThat(emitido.expiraEmSegundos()).isEqualTo(3600);
        assertThat(jwt.getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    void tokenExpiradoEhRecusado() {
        String expirado = token(encoder, "aguia-inova-api", Instant.now().minus(Duration.ofMinutes(10)));

        assertThatThrownBy(() -> decoder.decode(expirado)).isInstanceOf(JwtException.class);
    }

    @Test
    void tokenDeOutroEmissorEhRecusado() {
        String deOutro = token(encoder, "outro-sistema", Instant.now().plus(Duration.ofHours(1)));

        assertThatThrownBy(() -> decoder.decode(deOutro)).isInstanceOf(JwtException.class);
    }

    @Test
    void tokenAssinadoComOutraChaveEhRecusado() {
        SecretKey outraChave = new SecretKeySpec("outro-segredo-completamente-diferente!!".getBytes(StandardCharsets.UTF_8),
                "HmacSHA256");
        String falsificado = token(new NimbusJwtEncoder(new ImmutableSecret<>(outraChave)), "aguia-inova-api",
                Instant.now().plus(Duration.ofHours(1)));

        assertThatThrownBy(() -> decoder.decode(falsificado)).isInstanceOf(JwtException.class);
    }

    @Test
    void tokenSemAssinaturaEhRecusado() {
        String valido = jwtService.emitir(usuario()).valor();
        String semAssinatura = valido.substring(0, valido.lastIndexOf('.') + 1);

        assertThatThrownBy(() -> decoder.decode(semAssinatura)).isInstanceOf(JwtException.class);
    }

    @Test
    void segredoDeExatamente32BytesEAceito() {
        AppProperties limite = new AppProperties(new AppProperties.Jwt("a".repeat(32), 60, "x"), props.security(),
                props.seed(), props.ai(), props.rateLimit(), props.dashboard());

        assertThat(config.chaveJwt(limite).getEncoded()).hasSize(32);
    }

    @Test
    void segredoCurtoImpedeAInicializacao() {
        AppProperties fraca = new AppProperties(new AppProperties.Jwt("a".repeat(31), 60, "x"),
                props.security(), props.seed(), props.ai(), props.rateLimit(), props.dashboard());

        assertThatThrownBy(() -> config.chaveJwt(fraca))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 bytes");
    }

    @Test
    void segredoPadraoConhecidoImpedeAInicializacao() {
        AppProperties comSegredoPadraoDoApplicationYml = new AppProperties(
                new AppProperties.Jwt("aguia-inova-dev-secret-troque-em-producao-0123456789", 60, "x"),
                props.security(), props.seed(), props.ai(), props.rateLimit(), props.dashboard());
        AppProperties comSegredoPadraoDoDockerCompose = new AppProperties(
                new AppProperties.Jwt("dev-only-secret-troque-em-producao-min-32-bytes!", 60, "x"),
                props.security(), props.seed(), props.ai(), props.rateLimit(), props.dashboard());

        assertThatThrownBy(() -> config.chaveJwt(comSegredoPadraoDoApplicationYml))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("padrão");
        assertThatThrownBy(() -> config.chaveJwt(comSegredoPadraoDoDockerCompose))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("padrão");
    }
}
