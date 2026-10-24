package br.com.fiap.aguiainova.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.fiap.aguiainova.config.ClockConfig;
import br.com.fiap.aguiainova.dto.request.EstrategiaRequest;
import br.com.fiap.aguiainova.dto.response.EstrategiaHistoricoResponse;
import br.com.fiap.aguiainova.dto.response.EstrategiaResponse;
import br.com.fiap.aguiainova.exception.ConflitoException;
import br.com.fiap.aguiainova.exception.NaoEncontradoException;
import br.com.fiap.aguiainova.exception.RegraDeNegocioException;
import br.com.fiap.aguiainova.mapper.EstrategiaMapper;
import br.com.fiap.aguiainova.model.Estrategia;
import br.com.fiap.aguiainova.model.EstrategiaHistorico;
import br.com.fiap.aguiainova.model.enums.AcaoEstrategia;
import br.com.fiap.aguiainova.model.enums.StatusEstrategia;
import br.com.fiap.aguiainova.repository.EstrategiaHistoricoRepository;
import br.com.fiap.aguiainova.repository.EstrategiaRepository;
import br.com.fiap.aguiainova.repository.IdeiaRepository;
import br.com.fiap.aguiainova.repository.ProjetoRepository;
import br.com.fiap.aguiainova.security.UsuarioLogado;
import br.com.fiap.aguiainova.util.Consultas;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class EstrategiaService {

    private final EstrategiaRepository estrategias;
    private final EstrategiaHistoricoRepository historico;
    private final IdeiaRepository ideias;
    private final ProjetoRepository projetos;
    private final MongoTemplate mongo;
    private final EstrategiaMapper mapper;
    private final Clock clock;

    public EstrategiaService(EstrategiaRepository estrategias, EstrategiaHistoricoRepository historico,
            IdeiaRepository ideias, ProjetoRepository projetos, MongoTemplate mongo, EstrategiaMapper mapper,
            Clock clock) {
        this.estrategias = estrategias;
        this.historico = historico;
        this.ideias = ideias;
        this.projetos = projetos;
        this.mongo = mongo;
        this.mapper = mapper;
        this.clock = clock;
    }

    public List<EstrategiaResponse> listar(StatusEstrategia status, String categoria, String campanha) {
        Query consulta = new Query().with(Sort.by(Sort.Direction.DESC, "criadoEm"));
        Consultas.seIgual(consulta, "status", status);
        Consultas.seInformadoIgnorandoCaixa(consulta, "categoria", categoria);
        Consultas.seInformadoIgnorandoCaixa(consulta, "campanha", campanha);
        return mongo.find(consulta, Estrategia.class).stream().map(mapper::paraResponse).toList();
    }

    public EstrategiaResponse obter(String id) {
        return mapper.paraResponse(buscarEntidade(id));
    }

    public EstrategiaResponse criar(EstrategiaRequest req, UsuarioLogado autor) {
        Estrategia estrategia = estrategias.save(new Estrategia(mapper.paraDados(req), autor.comoAutor()));
        registrarHistorico(estrategia, AcaoEstrategia.CRIADA, autor);
        return mapper.paraResponse(estrategia);
    }

    public EstrategiaResponse atualizar(String id, EstrategiaRequest req, UsuarioLogado autor) {
        Estrategia estrategia = buscarEntidade(id);
        estrategia.atualizar(mapper.paraDados(req));
        estrategia = estrategias.save(estrategia);
        registrarHistorico(estrategia, AcaoEstrategia.ATUALIZADA, autor);
        return mapper.paraResponse(estrategia);
    }

    public void remover(String id, UsuarioLogado autor) {
        Estrategia estrategia = buscarEntidade(id);
        if (ideias.existsByEstrategiaId(id) || projetos.existsByEstrategiaId(id)) {
            throw new ConflitoException("A estratégia possui ideias ou projetos vinculados. "
                    + "Encerre-a (status ENCERRADA) em vez de excluir.");
        }
        estrategias.delete(estrategia);
        registrarHistorico(estrategia, AcaoEstrategia.REMOVIDA, autor);
    }

    public List<EstrategiaHistoricoResponse> historico(String estrategiaId, String categoria, String campanha,
            LocalDate de, LocalDate ate) {
        Query consulta = new Query().with(Sort.by(Sort.Direction.DESC, "data"));
        Consultas.seInformado(consulta, "estrategiaId", estrategiaId);
        Consultas.seInformadoIgnorandoCaixa(consulta, "categoria", categoria);
        Consultas.seInformadoIgnorandoCaixa(consulta, "campanha", campanha);
        if (de != null || ate != null) {
            consulta.addCriteria(periodo(de, ate));
        }
        return mongo.find(consulta, EstrategiaHistorico.class).stream().map(mapper::paraResponse).toList();
    }

    /**
     * Valida o vinculo de uma ideia/projeto com uma estrategia: precisa existir e estar vigente. Nao faz nada
     * quando nenhum id foi informado (o vinculo e opcional). Usado por outros servicos.
     */
    public void exigirVigente(String estrategiaId) {
        if (!Consultas.informado(estrategiaId)) {
            return;
        }
        Estrategia estrategia = estrategias.findById(estrategiaId)
                .orElseThrow(() -> new RegraDeNegocioException("A estratégia informada não existe."));
        if (!estrategia.estaVigente()) {
            throw new RegraDeNegocioException("A estratégia informada não está vigente.");
        }
    }

    /** Mapa id -> titulo, para exibir o nome da estrategia vinculada sem uma consulta por registro. */
    public Map<String, String> titulos(Collection<String> ids) {
        Map<String, String> titulos = new HashMap<>();
        List<String> pesquisados = ids.stream().filter(Consultas::informado).distinct().toList();
        if (!pesquisados.isEmpty()) {
            estrategias.findAllById(pesquisados).forEach(e -> titulos.put(e.getId(), e.getTitulo()));
        }
        return titulos;
    }

    private Estrategia buscarEntidade(String id) {
        return estrategias.findById(id).orElseThrow(() -> new NaoEncontradoException("Estratégia não encontrada."));
    }

    /** Intervalo [de, ate] inclusivo, em dias do fuso da empresa, sobre o campo {@code data}. */
    private static Criteria periodo(LocalDate de, LocalDate ate) {
        Criteria periodo = Criteria.where("data");
        if (de != null) {
            periodo = periodo.gte(de.atStartOfDay(ClockConfig.FUSO).toInstant());
        }
        if (ate != null) {
            periodo = periodo.lt(ate.plusDays(1).atStartOfDay(ClockConfig.FUSO).toInstant());
        }
        return periodo;
    }

    private void registrarHistorico(Estrategia estrategia, AcaoEstrategia acao, UsuarioLogado autor) {
        historico.save(EstrategiaHistorico.registrar(estrategia, acao, autor.comoAutor(), clock.instant()));
    }
}
