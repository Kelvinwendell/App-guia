package br.com.fiap.aguiainova.service;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import br.com.fiap.aguiainova.dto.request.AvaliacaoIdeiaRequest;
import br.com.fiap.aguiainova.dto.request.IdeiaRequest;
import br.com.fiap.aguiainova.dto.response.IdeiaResponse;
import br.com.fiap.aguiainova.exception.NaoEncontradoException;
import br.com.fiap.aguiainova.exception.ProibidoException;
import br.com.fiap.aguiainova.mapper.IdeiaMapper;
import br.com.fiap.aguiainova.model.DadosIdeia;
import br.com.fiap.aguiainova.model.Ideia;
import br.com.fiap.aguiainova.model.enums.Prioridade;
import br.com.fiap.aguiainova.model.enums.StatusIdeia;
import br.com.fiap.aguiainova.repository.IdeiaRepository;
import br.com.fiap.aguiainova.security.UsuarioLogado;
import br.com.fiap.aguiainova.util.Consultas;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class IdeiaService {

    private static final Sort MAIS_RECENTES = Sort.by(Sort.Direction.DESC, "criadoEm");

    private final IdeiaRepository ideias;
    private final EstrategiaService estrategias;
    private final MongoTemplate mongo;
    private final IdeiaMapper mapper;
    private final Clock clock;

    public IdeiaService(IdeiaRepository ideias, EstrategiaService estrategias, MongoTemplate mongo,
            IdeiaMapper mapper, Clock clock) {
        this.ideias = ideias;
        this.estrategias = estrategias;
        this.mongo = mongo;
        this.mapper = mapper;
        this.clock = clock;
    }

    /** OPERADOR enxerga apenas as proprias ideias; GESTOR enxerga todas. */
    public List<IdeiaResponse> listar(UsuarioLogado usuario, StatusIdeia status, Prioridade prioridade,
            String categoria, String estrategiaId) {
        Query consulta = new Query().with(MAIS_RECENTES);
        if (usuario.eOperador()) {
            consulta.addCriteria(Criteria.where("autor.usuarioId").is(usuario.id()));
        }
        if (status != null) {
            consulta.addCriteria(Criteria.where("status").is(status));
        }
        if (prioridade != null) {
            consulta.addCriteria(Criteria.where("prioridade").is(prioridade));
        }
        if (Consultas.informado(categoria)) {
            consulta.addCriteria(Consultas.igualIgnorandoCaixa("categoria", categoria));
        }
        if (Consultas.informado(estrategiaId)) {
            consulta.addCriteria(Criteria.where("estrategiaId").is(estrategiaId));
        }
        return paraResponses(mongo.find(consulta, Ideia.class), usuario);
    }

    public List<IdeiaResponse> listarAprovadas(UsuarioLogado usuario) {
        return paraResponses(ideias.findByStatus(StatusIdeia.APROVADA, MAIS_RECENTES), usuario);
    }

    public IdeiaResponse obter(String id, UsuarioLogado usuario) {
        return paraResponse(buscarVisivelPara(id, usuario), usuario);
    }

    public IdeiaResponse criar(IdeiaRequest req, UsuarioLogado autor) {
        DadosIdeia dados = mapper.paraDados(req);
        estrategias.exigirVigente(dados.estrategiaId());
        return paraResponse(ideias.save(new Ideia(dados, autor.comoAutor())), autor);
    }

    public IdeiaResponse atualizar(String id, IdeiaRequest req, UsuarioLogado autor) {
        Ideia ideia = buscarVisivelPara(id, autor);
        DadosIdeia dados = mapper.paraDados(req);
        if (dados.estrategiaId() != null && !dados.estrategiaId().equals(ideia.getEstrategiaId())) {
            estrategias.exigirVigente(dados.estrategiaId());
        }
        ideia.revisar(dados);
        return paraResponse(ideias.save(ideia), autor);
    }

    public void remover(String id, UsuarioLogado autor) {
        Ideia ideia = buscarVisivelPara(id, autor);
        ideia.validarRemocao();
        ideias.delete(ideia);
    }

    /** Decisao do gestor: aprovar/rejeitar, priorizar e comentar. */
    public IdeiaResponse avaliar(String id, AvaliacaoIdeiaRequest req, UsuarioLogado gestor) {
        Ideia ideia = buscarEntidade(id);
        ideia.avaliar(req.status(), req.prioridade(), req.parecer(), gestor.comoAutor(), clock.instant());
        return paraResponse(ideias.save(ideia), gestor);
    }

    private Ideia buscarEntidade(String id) {
        return ideias.findById(id).orElseThrow(() -> new NaoEncontradoException("Ideia não encontrada."));
    }

    /** Busca a ideia garantindo que um OPERADOR so alcance as proprias. */
    private Ideia buscarVisivelPara(String id, UsuarioLogado usuario) {
        Ideia ideia = buscarEntidade(id);
        if (usuario.eOperador() && !ideia.pertenceA(usuario.id())) {
            throw new ProibidoException("Você só pode acessar as suas próprias ideias.");
        }
        return ideia;
    }

    private IdeiaResponse paraResponse(Ideia ideia, UsuarioLogado usuario) {
        return paraResponses(List.of(ideia), usuario).get(0);
    }

    private List<IdeiaResponse> paraResponses(List<Ideia> lista, UsuarioLogado usuario) {
        Map<String, String> titulos = estrategias.titulos(
                lista.stream().map(Ideia::getEstrategiaId).filter(Objects::nonNull).toList());
        boolean incluirAvaliacaoIa = !usuario.eOperador();
        return lista.stream()
                .map(i -> mapper.paraResponse(i, titulos.get(i.getEstrategiaId()), incluirAvaliacaoIa))
                .toList();
    }
}
