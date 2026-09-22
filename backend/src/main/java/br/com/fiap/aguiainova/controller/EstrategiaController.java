package br.com.fiap.aguiainova.controller;

import java.time.LocalDate;
import java.util.List;

import br.com.fiap.aguiainova.dto.request.EstrategiaRequest;
import br.com.fiap.aguiainova.dto.response.EstrategiaHistoricoResponse;
import br.com.fiap.aguiainova.dto.response.EstrategiaResponse;
import br.com.fiap.aguiainova.model.enums.StatusEstrategia;
import br.com.fiap.aguiainova.security.UsuarioLogado;
import br.com.fiap.aguiainova.service.EstrategiaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estrategias")
@Tag(name = "Estratégias", description = "Orientações estratégicas: LIDERANCA gerencia (CRUD); todos os perfis consultam")
public class EstrategiaController {

    private final EstrategiaService estrategias;

    public EstrategiaController(EstrategiaService estrategias) {
        this.estrategias = estrategias;
    }

    @GetMapping
    @Operation(summary = "Lista estratégias (filtros opcionais: status, categoria, campanha)")
    public List<EstrategiaResponse> listar(@RequestParam(required = false) StatusEstrategia status,
            @RequestParam(required = false) String categoria, @RequestParam(required = false) String campanha) {
        return estrategias.listar(status, categoria, campanha);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha uma estratégia")
    public EstrategiaResponse obter(@PathVariable String id) {
        return estrategias.obter(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('LIDERANCA')")
    @Operation(summary = "Cria uma estratégia (LIDERANCA)")
    public EstrategiaResponse criar(@Valid @RequestBody EstrategiaRequest req,
            @AuthenticationPrincipal UsuarioLogado autor) {
        return estrategias.criar(req, autor);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LIDERANCA')")
    @Operation(summary = "Atualiza uma estratégia (LIDERANCA)")
    public EstrategiaResponse atualizar(@PathVariable String id, @Valid @RequestBody EstrategiaRequest req,
            @AuthenticationPrincipal UsuarioLogado autor) {
        return estrategias.atualizar(id, req, autor);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('LIDERANCA')")
    @Operation(summary = "Remove uma estratégia sem vínculos (LIDERANCA)")
    public void remover(@PathVariable String id, @AuthenticationPrincipal UsuarioLogado autor) {
        estrategias.remover(id, autor);
    }

    @GetMapping("/historico")
    @Operation(summary = "Histórico de mudanças das estratégias (id, data, categoria, campanha)")
    public List<EstrategiaHistoricoResponse> historico(@RequestParam(required = false) String estrategiaId,
            @RequestParam(required = false) String categoria, @RequestParam(required = false) String campanha,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        return estrategias.historico(estrategiaId, categoria, campanha, de, ate);
    }

    @GetMapping("/{id}/historico")
    @Operation(summary = "Histórico de uma estratégia específica (inclui as já removidas)")
    public List<EstrategiaHistoricoResponse> historicoDaEstrategia(@PathVariable String id) {
        return estrategias.historico(id, null, null, null, null);
    }
}
