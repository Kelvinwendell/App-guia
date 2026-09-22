package br.com.fiap.aguiainova.mapper;

import br.com.fiap.aguiainova.dto.request.RegistroRequest;
import br.com.fiap.aguiainova.dto.response.UsuarioResponse;
import br.com.fiap.aguiainova.model.DadosUsuario;
import br.com.fiap.aguiainova.model.Usuario;
import br.com.fiap.aguiainova.model.enums.Perfil;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    /** A senha em texto puro nunca entra no dominio: o servico entrega apenas o hash. */
    public DadosUsuario paraDados(RegistroRequest req, String senhaHash, Perfil perfil) {
        return new DadosUsuario(req.nome(), req.email(), senhaHash, perfil, req.cargo(), req.departamento(),
                req.unidade());
    }

    public UsuarioResponse paraResponse(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil(),
                usuario.getCargo(), usuario.getDepartamento(), usuario.getUnidade(), usuario.isAtivo());
    }
}
