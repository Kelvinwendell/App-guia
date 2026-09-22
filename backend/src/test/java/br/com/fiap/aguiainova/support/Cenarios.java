package br.com.fiap.aguiainova.support;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.com.fiap.aguiainova.dto.request.AvaliacaoIdeiaRequest;
import br.com.fiap.aguiainova.dto.request.EstrategiaRequest;
import br.com.fiap.aguiainova.dto.request.IdeiaRequest;
import br.com.fiap.aguiainova.dto.request.ProjetoRequest;
import br.com.fiap.aguiainova.dto.request.ResultadoRequest;
import br.com.fiap.aguiainova.model.enums.Prioridade;
import br.com.fiap.aguiainova.model.enums.StatusEstrategia;
import br.com.fiap.aguiainova.model.enums.StatusIdeia;
import br.com.fiap.aguiainova.model.enums.StatusProjeto;
import br.com.fiap.aguiainova.security.UsuarioLogado;
import br.com.fiap.aguiainova.service.EstrategiaService;
import br.com.fiap.aguiainova.service.IdeiaService;
import br.com.fiap.aguiainova.service.ProjetoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;

/** Monta dados de teste pelos mesmos servicos da aplicacao (regras e historico incluidos). */
@TestComponent
public class Cenarios {

    @Autowired
    private EstrategiaService estrategias;
    @Autowired
    private IdeiaService ideias;
    @Autowired
    private ProjetoService projetos;

    public String estrategia(UsuarioLogado lideranca, String titulo, String categoria, String campanha) {
        return estrategias.criar(new EstrategiaRequest(titulo, "Descrição de " + titulo, categoria, campanha, null),
                lideranca).id();
    }

    public String estrategiaEncerrada(UsuarioLogado lideranca, String titulo) {
        return estrategias.criar(new EstrategiaRequest(titulo, "Descrição", "Geral", null,
                StatusEstrategia.ENCERRADA), lideranca).id();
    }

    public String ideia(UsuarioLogado operador, String titulo, String estrategiaId) {
        return ideias.criar(new IdeiaRequest(titulo, "Descrição detalhada de " + titulo, "Tecnologia", estrategiaId),
                operador).id();
    }

    public String ideiaAprovada(UsuarioLogado operador, UsuarioLogado gestor, String titulo) {
        String id = ideia(operador, titulo, null);
        ideias.avaliar(id, new AvaliacaoIdeiaRequest(StatusIdeia.APROVADA, Prioridade.ALTA, null), gestor);
        return id;
    }

    public void rejeitar(String ideiaId, UsuarioLogado gestor) {
        ideias.avaliar(ideiaId, new AvaliacaoIdeiaRequest(StatusIdeia.REJEITADA, null, "Fora do escopo"), gestor);
    }

    public String projeto(UsuarioLogado gestor, String nome, String investimento, LocalDate prazo,
            StatusProjeto status, String estrategiaId) {
        return projeto(gestor, nome, investimento, null, prazo, status, estrategiaId);
    }

    public String projeto(UsuarioLogado gestor, String nome, String investimento, LocalDate inicio, LocalDate prazo,
            StatusProjeto status, String estrategiaId) {
        return projetos.criar(new ProjetoRequest(nome, "Descrição de " + nome, new BigDecimal(investimento), inicio,
                prazo, estrategiaId, null, status, null), gestor).id();
    }

    public void resultado(String projetoId, UsuarioLogado gestor, LocalDate data, String retorno,
            Double produtividade, Double reducaoCustos) {
        projetos.adicionarResultado(projetoId, new ResultadoRequest(data, "Resultado", new BigDecimal(retorno),
                produtividade, reducaoCustos), gestor);
    }
}
