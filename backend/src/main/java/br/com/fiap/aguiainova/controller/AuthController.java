package br.com.fiap.aguiainova.controller;

import br.com.fiap.aguiainova.dto.request.LoginRequest;
import br.com.fiap.aguiainova.dto.request.RegistroRequest;
import br.com.fiap.aguiainova.dto.response.AuthResponse;
import br.com.fiap.aguiainova.dto.response.UsuarioResponse;
import br.com.fiap.aguiainova.security.UsuarioLogado;
import br.com.fiap.aguiainova.service.AuthService;
import br.com.fiap.aguiainova.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Cadastro, login (JWT) e dados do usuário logado")
public class AuthController {

    private final AuthService auth;
    private final UsuarioService usuarios;

    public AuthController(AuthService auth, UsuarioService usuarios) {
        this.auth = auth;
        this.usuarios = usuarios;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um novo usuário (público)")
    public UsuarioResponse registrar(@Valid @RequestBody RegistroRequest req) {
        return usuarios.registrar(req);
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica e devolve o token JWT (Bearer)")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return auth.login(req);
    }

    @GetMapping("/me")
    @Operation(summary = "Dados do usuário autenticado")
    public UsuarioResponse eu(@AuthenticationPrincipal UsuarioLogado logado) {
        return usuarios.obter(logado.id());
    }
}
