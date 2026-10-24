package br.com.fiap.aguiainova.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import br.com.fiap.aguiainova.dto.request.ProgressoRequest;
import br.com.fiap.aguiainova.dto.request.ProjetoRequest;
import br.com.fiap.aguiainova.dto.request.ResultadoRequest;
import br.com.fiap.aguiainova.dto.response.ProjetoResponse;
import br.com.fiap.aguiainova.exception.NaoEncontradoException;
import br.com.fiap.aguiainova.exception.RegraDeNegocioException;
import br.com.fiap.aguiainova.mapper.ProjetoMapper;
import br.com.fiap.aguiainova.model.DadosProjeto;
import br.com.fiap.aguiainova.model.Ideia;
import br.com.fiap.aguiainova.model.Projeto;
import br.com.fiap.aguiainova.model.enums.EtapaProjeto;
import br.com.fiap.aguiainova.model.enums.StatusIdeia;
import br.com.fiap.aguiainova.model.enums.StatusProjeto;
import br.com.fiap.aguiainova.repository.IdeiaRepository;
import br.com.fiap.aguiainova.repository.ProjetoRepository;
import br.com.fiap.aguiainova.security.UsuarioLogado;
import br.com.fiap.aguiainova.util.Consultas;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class ProjetoService {

    private final ProjetoRepository projetos;
    private final IdeiaRepository ideias;
    private final EstrategiaService estrategias;
    private final MongoTemplate mongo;
    private final ProjetoMapper mapper;
    private final Clock clock;

    public ProjetoService(ProjetoRepository projetos, IdeiaRepository ideias, EstrategiaService estrategias,
            MongoTemplate mongo, ProjetoMapper mapper, Clock clock) {
        this.projetos = projetos;
        this.ideias = ideias;
        this.estrategias = estrategias;
        this.mongo = mongo;
        this.mapper = mapper;
        this.clock = clock;
    }

    public List<ProjetoResponse> listar(StatusProjeto status, EtapaProjeto etapa, String estrategiaId) {
        Query consulta = new Query().with(Sort.by(Sort.Direction.DESC, "criadoEm"));
        Consultas.seIgual(consulta, "status", status);
        Consultas.seIgual(consulta, "etapa", etapa);
        Consultas.seInformado(consulta, "estrategiaId", estrategiaId);
        return paraResponses(mongo.find(consulta, Projeto.class));
    }

    public ProjetoResponse obter(String id) {
        return paraResponse(buscarEntidade(id));
    }

    public ProjetoResponse criar(ProjetoRequest req, UsuarioLogado gestor) {
        DadosProjeto dados = mapper.paraDados(req, hoje());
        estrategias.exigirVigente(dados.estrategiaId());
        exigirIdeiaAprovada(dados.ideiaId());
        return paraResponse(projetos.save(new Projeto(dados, gestor.comoAutor())));
    }

    public ProjetoResponse atualizar(String id, ProjetoRequest req) {
        Projeto projeto = buscarEntidade(id);
        DadosProjeto dados = mapper.paraDados(req, hoje());
        // Estrategia e ideia so sao revalidadas quando mudam, para nao bloquear a edicao de projetos antigos.
        if (!Objects.equals(dados.estrategiaId(), projeto.getEstrategiaId())) {
            estrategias.exigirVigente(dados.estrategiaId());
        }
        if (!Objects.equals(dados.ideiaId(), projeto.getIdeiaId())) {
            exigirIdeiaAprovada(dados.ideiaId());
        }
        projeto.atualizarDados(dados);
        return paraResponse(projetos.save(projeto));
    }

    public ProjetoResponse atualizarProgresso(String id, ProgressoRequest req) {
        Projeto projeto = buscarEntidade(id);
        projeto.atualizarAndamento(req.status(), req.etapa(), req.progresso());
        return paraResponse(projetos.save(projeto));
    }

    public ProjetoResponse adicionarResultado(String id, ResultadoRequest req, UsuarioLogado gestor) {
        Projeto projeto = buscarEntidade(id);
        projeto.registrarResultado(mapper.paraDadosResultado(req, hoje()), gestor.comoAutor());
        return paraResponse(projetos.save(projeto));
    }

    public ProjetoResponse removerResultado(String id, String resultadoId) {
        Projeto projeto = buscarEntidade(id);
        projeto.removerResultado(resultadoId);
        return paraResponse(projetos.save(projeto));
    }

    public void remover(String id) {
        projetos.delete(buscarEntidade(id));
    }

    private Projeto buscarEntidade(String id) {
        return projetos.findById(id).orElseThrow(() -> new NaoEncontradoException("Projeto não encontrado."));
    }

    private LocalDate hoje() {
        return LocalDate.now(clock);
    }

    private void exigirIdeiaAprovada(String ideiaId) {
        if (ideiaId == null) {
            return;
        }
        Ideia ideia = ideias.findById(ideiaId)
                .orElseThrow(() -> new RegraDeNegocioException("A ideia informada não existe."));
        if (ideia.getStatus() != StatusIdeia.APROVADA) {
            throw new RegraDeNegocioException("Só é possível criar projetos a partir de ideias aprovadas.");
        }
    }

    private ProjetoResponse paraResponse(Projeto projeto) {
        return paraResponses(List.of(projeto)).get(0);
    }

    private List<ProjetoResponse> paraResponses(List<Projeto> lista) {
        Map<String, String> titulosEstrategias = estrategias.titulos(
                lista.stream().map(Projeto::getEstrategiaId).filter(Objects::nonNull).toList());
        Map<String, String> titulosIdeias = titulosDasIdeias(lista);
        LocalDate hoje = hoje();
        return lista.stream()
                .map(p -> mapper.paraResponse(p, titulosEstrategias.get(p.getEstrategiaId()),
                        titulosIdeias.get(p.getIdeiaId()), hoje))
                .toList();
    }

    private Map<String, String> titulosDasIdeias(List<Projeto> lista) {
        List<String> ids = lista.stream().map(Projeto::getIdeiaId).filter(Objects::nonNull).distinct().toList();
        Map<String, String> titulos = new HashMap<>();
        if (!ids.isEmpty()) {
            ideias.findAllById(ids).forEach(ideia -> titulos.put(ideia.getId(), ideia.getTitulo()));
        }
        return titulos;
    }
}
