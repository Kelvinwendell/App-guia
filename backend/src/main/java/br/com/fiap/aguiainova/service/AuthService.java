package br.com.fiap.aguiainova.service;

import br.com.fiap.aguiainova.dto.request.LoginRequest;
import br.com.fiap.aguiainova.dto.response.AuthResponse;
import br.com.fiap.aguiainova.exception.NaoAutenticadoException;
import br.com.fiap.aguiainova.mapper.UsuarioMapper;
import br.com.fiap.aguiainova.model.Usuario;
import br.com.fiap.aguiainova.repository.UsuarioRepository;
import br.com.fiap.aguiainova.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final String MENSAGEM_CREDENCIAIS_INVALIDAS = "E-mail ou senha inválidos.";
    private static final String TIPO_TOKEN = "Bearer";

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final UsuarioMapper mapper;
    /** Hash descartavel: permite gastar o mesmo tempo de BCrypt quando o e-mail nao existe (evita enumeracao). */
    private final String hashFalso;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder, JwtService jwt, UsuarioMapper mapper) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.jwt = jwt;
        this.mapper = mapper;
        this.hashFalso = encoder.encode("senha-inexistente");
    }

    public AuthResponse login(LoginRequest req) {
        Usuario usuario = usuarios.findByEmail(UsuarioService.normalizarEmail(req.email())).orElse(null);
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
