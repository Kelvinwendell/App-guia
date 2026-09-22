package br.com.fiap.aguiainova.controller;

import java.util.List;

import br.com.fiap.aguiainova.dto.request.ProgressoRequest;
import br.com.fiap.aguiainova.dto.request.ProjetoRequest;
import br.com.fiap.aguiainova.dto.request.ResultadoRequest;
import br.com.fiap.aguiainova.dto.response.ProjetoResponse;
import br.com.fiap.aguiainova.model.enums.EtapaProjeto;
import br.com.fiap.aguiainova.model.enums.StatusProjeto;
import br.com.fiap.aguiainova.security.UsuarioLogado;
import br.com.fiap.aguiainova.service.ProjetoService;
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
@RequestMapping("/api/projetos")
@Tag(name = "Projetos", description = "GESTOR: CRUD, progresso e resultados. LIDERANCA: consulta o andamento")
public class ProjetoController {

    private final ProjetoService projetos;

    public ProjetoController(ProjetoService projetos) {
        this.projetos = projetos;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('GESTOR','LIDERANCA')")
    @Operation(summary = "Lista projetos (filtros: status, etapa, estrategiaId)")
    public List<ProjetoResponse> listar(@RequestParam(required = false) StatusProjeto status,
            @RequestParam(required = false) EtapaProjeto etapa,
            @RequestParam(required = false) String estrategiaId) {
        return projetos.listar(status, etapa, estrategiaId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GESTOR','LIDERANCA')")
    @Operation(summary = "Detalha um projeto com indicadores e resultados")
    public ProjetoResponse obter(@PathVariable String id) {
        return projetos.obter(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Cadastra um projeto/iniciativa (GESTOR)")
    public ProjetoResponse criar(@Valid @RequestBody ProjetoRequest req, @AuthenticationPrincipal UsuarioLogado gestor) {
        return projetos.criar(req, gestor);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Atualiza os dados de um projeto (GESTOR)")
    public ProjetoResponse atualizar(@PathVariable String id, @Valid @RequestBody ProjetoRequest req) {
        return projetos.atualizar(id, req);
    }

    @PatchMapping("/{id}/progresso")
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Atualiza status, etapa e/ou progresso (GESTOR)")
    public ProjetoResponse atualizarProgresso(@PathVariable String id, @Valid @RequestBody ProgressoRequest req) {
        return projetos.atualizarProgresso(id, req);
    }

    @PostMapping("/{id}/resultados")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Registra um resultado obtido pelo projeto (GESTOR)")
    public ProjetoResponse adicionarResultado(@PathVariable String id, @Valid @RequestBody ResultadoRequest req,
            @AuthenticationPrincipal UsuarioLogado gestor) {
        return projetos.adicionarResultado(id, req, gestor);
    }

    @DeleteMapping("/{id}/resultados/{resultadoId}")
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Remove um resultado registrado (GESTOR)")
    public ProjetoResponse removerResultado(@PathVariable String id, @PathVariable String resultadoId) {
        return projetos.removerResultado(id, resultadoId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Remove um projeto (GESTOR)")
    public void remover(@PathVariable String id) {
        projetos.remover(id);
    }
}
