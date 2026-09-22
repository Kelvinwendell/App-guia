package br.com.fiap.aguiainova.controller;

import java.util.List;

import br.com.fiap.aguiainova.dto.request.AlterarPerfilRequest;
import br.com.fiap.aguiainova.dto.response.UsuarioResponse;
import br.com.fiap.aguiainova.security.UsuarioLogado;
import br.com.fiap.aguiainova.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('LIDERANCA')")
@Tag(name = "Usuários", description = "Administração de usuários (somente LIDERANCA)")
public class UsuarioController {

    private final UsuarioService usuarios;

    public UsuarioController(UsuarioService usuarios) {
        this.usuarios = usuarios;
    }

    @GetMapping
    @Operation(summary = "Lista todos os usuários")
    public List<UsuarioResponse> listar() {
        return usuarios.listar();
    }

    @PatchMapping("/{id}/perfil")
    @Operation(summary = "Altera o perfil (nível de acesso) de um usuário")
    public UsuarioResponse alterarPerfil(@PathVariable String id, @Valid @RequestBody AlterarPerfilRequest req,
            @AuthenticationPrincipal UsuarioLogado autor) {
        return usuarios.alterarPerfil(id, req.perfil(), autor);
    }
}
