package br.com.fiap.aguiainova.service;

import java.util.List;
import java.util.Locale;

import br.com.fiap.aguiainova.config.AppProperties;
import br.com.fiap.aguiainova.dto.request.RegistroRequest;
import br.com.fiap.aguiainova.dto.response.UsuarioResponse;
import br.com.fiap.aguiainova.exception.ConflitoException;
import br.com.fiap.aguiainova.exception.NaoEncontradoException;
import br.com.fiap.aguiainova.exception.RegraDeNegocioException;
import br.com.fiap.aguiainova.mapper.UsuarioMapper;
import br.com.fiap.aguiainova.model.Usuario;
import br.com.fiap.aguiainova.model.enums.Perfil;
import br.com.fiap.aguiainova.repository.UsuarioRepository;
import br.com.fiap.aguiainova.security.UsuarioLogado;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final AppProperties props;
    private final UsuarioMapper mapper;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder encoder, AppProperties props,
            UsuarioMapper mapper) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.props = props;
        this.mapper = mapper;
    }

    public static String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public UsuarioResponse registrar(RegistroRequest req) {
        if (usuarios.existsByEmail(normalizarEmail(req.email()))) {
            throw new ConflitoException("Já existe um usuário cadastrado com este e-mail.");
        }
        var dados = mapper.paraDados(req, encoder.encode(req.senha()), perfilPermitido(req.perfil()));
        return mapper.paraResponse(usuarios.save(new Usuario(dados)));
    }

    public UsuarioResponse obter(String id) {
        return mapper.paraResponse(buscarEntidade(id));
    }

    public List<UsuarioResponse> listar() {
        return usuarios.findAll(Sort.by("nome")).stream().map(mapper::paraResponse).toList();
    }

    public UsuarioResponse alterarPerfil(String id, Perfil novoPerfil, UsuarioLogado autor) {
        if (id.equals(autor.id())) {
            throw new RegraDeNegocioException("Você não pode alterar o seu próprio perfil.");
        }
        Usuario usuario = buscarEntidade(id);
        usuario.alterarPerfil(novoPerfil);
        return mapper.paraResponse(usuarios.save(usuario));
    }

    /** No cadastro publico o perfil pedido so vale se a escolha estiver habilitada; senao, OPERADOR. */
    private Perfil perfilPermitido(Perfil solicitado) {
        boolean podeEscolher = props.security().allowRoleSelection();
        return podeEscolher && solicitado != null ? solicitado : Perfil.OPERADOR;
    }

    private Usuario buscarEntidade(String id) {
        return usuarios.findById(id).orElseThrow(() -> new NaoEncontradoException("Usuário não encontrado."));
    }
}
