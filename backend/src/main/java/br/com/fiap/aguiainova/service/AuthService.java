package br.com.fiap.aguiainova.service;

import java.time.Duration;

import br.com.fiap.aguiainova.config.AppProperties;
import br.com.fiap.aguiainova.dto.request.LoginRequest;
import br.com.fiap.aguiainova.dto.response.AuthResponse;
import br.com.fiap.aguiainova.exception.LimiteDeRequisicoesExcedidoException;
import br.com.fiap.aguiainova.exception.NaoAutenticadoException;
import br.com.fiap.aguiainova.mapper.UsuarioMapper;
import br.com.fiap.aguiainova.model.Usuario;
import br.com.fiap.aguiainova.repository.UsuarioRepository;
import br.com.fiap.aguiainova.security.JwtService;
import br.com.fiap.aguiainova.security.LimitadorDeTaxa;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final String MENSAGEM_CREDENCIAIS_INVALIDAS = "E-mail ou senha inválidos.";
    private static final String MENSAGEM_LIMITE_EXCEDIDO = "Muitas tentativas de login. Tente novamente em instantes.";
    private static final String TIPO_TOKEN = "Bearer";

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final UsuarioMapper mapper;
    private final LimitadorDeTaxa limitador;
    private final int limiteLoginPorMinuto;
    /** Hash descartavel: permite gastar o mesmo tempo de BCrypt quando o e-mail nao existe (evita enumeracao). */
    private final String hashFalso;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder, JwtService jwt, UsuarioMapper mapper,
            LimitadorDeTaxa limitador, AppProperties props) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.jwt = jwt;
        this.mapper = mapper;
        this.limitador = limitador;
        this.limiteLoginPorMinuto = props.rateLimit().loginPorMinuto();
        this.hashFalso = encoder.encode("senha-inexistente");
    }

    public AuthResponse login(LoginRequest req) {
        String email = UsuarioService.normalizarEmail(req.email());
        if (!limitador.permitir("login:" + email, limiteLoginPorMinuto, Duration.ofMinutes(1))) {
            throw new LimiteDeRequisicoesExcedidoException(MENSAGEM_LIMITE_EXCEDIDO);
        }
        Usuario usuario = usuarios.findByEmail(email).orElse(null);
        String hash = usuario != null ? usuario.getSenhaHash() : hashFalso;
        boolean senhaConfere = encoder.matches(req.senha(), hash);
        if (usuario == null || !senhaConfere || !usuario.isAtivo()) {
            throw new NaoAutenticadoException(MENSAGEM_CREDENCIAIS_INVALIDAS);
        }
        JwtService.TokenEmitido token = jwt.emitir(usuario);
        return new AuthResponse(token.valor(), TIPO_TOKEN, token.expiraEmSegundos(), token.expiraEm(),
                mapper.paraResponse(usuario));
    }
}
