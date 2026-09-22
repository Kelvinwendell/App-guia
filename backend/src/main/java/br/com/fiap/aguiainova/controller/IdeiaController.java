package br.com.fiap.aguiainova.controller;

import java.util.List;

import br.com.fiap.aguiainova.dto.request.AvaliacaoIdeiaRequest;
import br.com.fiap.aguiainova.dto.request.IdeiaRequest;
import br.com.fiap.aguiainova.dto.response.IdeiaResponse;
import br.com.fiap.aguiainova.model.enums.Prioridade;
import br.com.fiap.aguiainova.model.enums.StatusIdeia;
import br.com.fiap.aguiainova.security.UsuarioLogado;
import br.com.fiap.aguiainova.service.IdeiaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ideias")
@Tag(name = "Ideias", description = "OPERADOR: CRUD das próprias ideias. GESTOR: consulta, prioriza e aprova/rejeita")
public class IdeiaController {

    private final IdeiaService ideias;

    public IdeiaController(IdeiaService ideias) {
        this.ideias = ideias;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERADOR','GESTOR')")
    @Operation(summary = "Lista ideias: o OPERADOR vê só as suas; o GESTOR vê todas")
    public List<IdeiaResponse> listar(@AuthenticationPrincipal UsuarioLogado usuario,
            @RequestParam(required = false) StatusIdeia status,
            @RequestParam(required = false) Prioridade prioridade,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String estrategiaId) {
        return ideias.listar(usuario, status, prioridade, categoria, estrategiaId);
    }

    @GetMapping("/aprovadas")
    @Operation(summary = "Ideias aprovadas da empresa (todos os perfis)")
    public List<IdeiaResponse> aprovadas(@AuthenticationPrincipal UsuarioLogado usuario) {
        return ideias.listarAprovadas(usuario);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERADOR','GESTOR')")
    @Operation(summary = "Detalha uma ideia")
    public IdeiaResponse obter(@PathVariable String id, @AuthenticationPrincipal UsuarioLogado usuario) {
        return ideias.obter(id, usuario);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Cadastra uma ideia (OPERADOR); nasce EM_ANALISE")
    public IdeiaResponse criar(@Valid @RequestBody IdeiaRequest req, @AuthenticationPrincipal UsuarioLogado usuario) {
        return ideias.criar(req, usuario);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Edita a própria ideia enquanto ela está EM_ANALISE (OPERADOR)")
    public IdeiaResponse atualizar(@PathVariable String id, @Valid @RequestBody IdeiaRequest req,
            @AuthenticationPrincipal UsuarioLogado usuario) {
        return ideias.atualizar(id, req, usuario);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Exclui a própria ideia enquanto ela está EM_ANALISE (OPERADOR)")
    public void remover(@PathVariable String id, @AuthenticationPrincipal UsuarioLogado usuario) {
        ideias.remover(id, usuario);
    }

    @PatchMapping("/{id}/avaliacao")
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Aprova/rejeita, prioriza e comenta uma ideia (GESTOR)")
    public IdeiaResponse avaliar(@PathVariable String id, @Valid @RequestBody AvaliacaoIdeiaRequest req,
            @AuthenticationPrincipal UsuarioLogado usuario) {
        return ideias.avaliar(id, req, usuario);
    }
}
