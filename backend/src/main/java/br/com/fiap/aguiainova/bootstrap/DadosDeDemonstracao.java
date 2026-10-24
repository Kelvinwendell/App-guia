package br.com.fiap.aguiainova.bootstrap;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import br.com.fiap.aguiainova.bootstrap.RoteiroDemo.EstrategiaDemo;
import br.com.fiap.aguiainova.bootstrap.RoteiroDemo.IdeiaDemo;
import br.com.fiap.aguiainova.bootstrap.RoteiroDemo.ProjetoDemo;
import br.com.fiap.aguiainova.bootstrap.RoteiroDemo.ResultadoDemo;
import br.com.fiap.aguiainova.bootstrap.RoteiroDemo.UsuarioDemo;
import br.com.fiap.aguiainova.config.AppProperties;
import br.com.fiap.aguiainova.config.ClockConfig;
import br.com.fiap.aguiainova.dto.request.AvaliacaoIdeiaRequest;
import br.com.fiap.aguiainova.dto.request.EstrategiaRequest;
import br.com.fiap.aguiainova.dto.request.IdeiaRequest;
import br.com.fiap.aguiainova.dto.request.ProgressoRequest;
import br.com.fiap.aguiainova.dto.request.ProjetoRequest;
import br.com.fiap.aguiainova.dto.request.ResultadoRequest;
import br.com.fiap.aguiainova.model.DadosUsuario;
import br.com.fiap.aguiainova.model.Estrategia;
import br.com.fiap.aguiainova.model.EstrategiaHistorico;
import br.com.fiap.aguiainova.model.Ideia;
import br.com.fiap.aguiainova.model.Projeto;
import br.com.fiap.aguiainova.model.Usuario;
import br.com.fiap.aguiainova.model.enums.StatusIdeia;
import br.com.fiap.aguiainova.repository.UsuarioRepository;
import br.com.fiap.aguiainova.security.UsuarioLogado;
import br.com.fiap.aguiainova.service.EstrategiaService;
import br.com.fiap.aguiainova.service.IdeiaService;
import br.com.fiap.aguiainova.service.ProjetoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Carrega (ou remove) a base de demonstracao descrita em {@link RoteiroDemo}, para apresentacoes. Tudo e criado pelos
 * mesmos servicos da API, entao regras, historico e vinculos ficam consistentes. Ao subir, olha
 * {@code app.seed.demo.modo}: "carregar" cria os dados uma unica vez (repetir nao duplica); "remover" apaga apenas o
 * que foi criado pelas contas de demonstracao, sem tocar em usuarios ou dados reais.
 */
@Component
public class DadosDeDemonstracao implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DadosDeDemonstracao.class);
    private static final int TAMANHO_MINIMO_DA_SENHA = 6;

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final EstrategiaService estrategias;
    private final IdeiaService ideias;
    private final ProjetoService projetos;
    private final MongoTemplate mongo;
    private final Clock clock;
    private final AppProperties props;

    public DadosDeDemonstracao(UsuarioRepository usuarios, PasswordEncoder encoder, EstrategiaService estrategias,
            IdeiaService ideias, ProjetoService projetos, MongoTemplate mongo, Clock clock, AppProperties props) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.estrategias = estrategias;
        this.ideias = ideias;
        this.projetos = projetos;
        this.mongo = mongo;
        this.clock = clock;
        this.props = props;
    }

    @Override
    public void run(ApplicationArguments args) {
        AppProperties.Seed.Demo demo = props.seed().demo();
        if (demo.remover()) {
            remover();
        } else if (demo.carregar()) {
            if (demo.senha() == null || demo.senha().strip().length() < TAMANHO_MINIMO_DA_SENHA) {
                log.error("Dados de demonstração NÃO carregados: defina APP_SEED_DEMO_SENHA com pelo menos {} caracteres.",
                        TAMANHO_MINIMO_DA_SENHA);
                return;
            }
            carregar(demo.senha().strip());
        }
    }

    /** Cria as contas, estrategias, ideias e projetos de demonstracao; nao faz nada se ja foram criados. */
    public void carregar(String senha) {
        if (usuarios.existsByEmail(RoteiroDemo.EMAIL_DA_LIDERANCA)) {
            log.info("Os dados de demonstração já estão carregados; nada foi alterado.");
            return;
        }
        log.warn("Carregando dados de demonstração (app.seed.demo.modo). Desligue depois da apresentação.");
        LocalDate hoje = LocalDate.now(clock);
        Map<String, UsuarioLogado> pessoas = criarUsuarios(senha);
        UsuarioLogado lideranca = pessoas.get("lideranca");
        UsuarioLogado gestor = pessoas.get("gestor");

        Map<String, String> idsDasEstrategias = criarEstrategias(lideranca, hoje);
        Map<String, String> idsDasIdeias = criarIdeias(pessoas, gestor, idsDasEstrategias);
        criarProjetos(gestor, hoje, idsDasEstrategias, idsDasIdeias);
        log.info("Dados de demonstração carregados: {} contas, {} estratégias, {} ideias e {} projetos.",
                pessoas.size(), idsDasEstrategias.size(), idsDasIdeias.size(), RoteiroDemo.projetos().size());
    }

    /** Apaga tudo o que as contas de demonstracao criaram e as proprias contas. */
    public void remover() {
        List<Usuario> contas = RoteiroDemo.emails().stream()
                .map(usuarios::findByEmail)
                .flatMap(Optional::stream)
                .toList();
        if (contas.isEmpty()) {
            log.info("Não há dados de demonstração para remover.");
            return;
        }
        List<String> ids = contas.stream().map(Usuario::getId).toList();
        Query criadosPelasContas = Query.query(Criteria.where("autor.usuarioId").in(ids));
        mongo.remove(criadosPelasContas, Ideia.class);
        mongo.remove(criadosPelasContas, Projeto.class);
        mongo.remove(criadosPelasContas, Estrategia.class);
        mongo.remove(criadosPelasContas, EstrategiaHistorico.class);
        usuarios.deleteAll(contas);
        log.warn("Dados de demonstração removidos ({} contas e tudo o que elas criaram).", contas.size());
    }

    private Map<String, UsuarioLogado> criarUsuarios(String senha) {
        String hash = encoder.encode(senha);
        Map<String, UsuarioLogado> pessoas = new HashMap<>();
        for (UsuarioDemo conta : RoteiroDemo.usuarios()) {
            Usuario salvo = usuarios.save(new Usuario(new DadosUsuario(conta.nome(), conta.email(), hash,
                    conta.perfil(), conta.cargo(), conta.departamento(), conta.unidade())));
            pessoas.put(conta.chave(), new UsuarioLogado(salvo.getId(), salvo.getNome(), salvo.getEmail(),
                    salvo.getPerfil()));
        }
        return pessoas;
    }

    private Map<String, String> criarEstrategias(UsuarioLogado lideranca, LocalDate hoje) {
        Map<String, String> ids = new HashMap<>();
        for (EstrategiaDemo e : RoteiroDemo.estrategias()) {
            var requisicao = new EstrategiaRequest(e.titulo(), e.descricao(), e.categoria(), e.campanha(), e.status(),
                    e.metas(), hoje.plusDays(e.inicioEmDias()), hoje.plusDays(e.fimEmDias()));
            String id = estrategias.criar(requisicao, lideranca).id();
            ids.put(e.chave(), id);
            if (e.descricaoRevisada() != null) {
                estrategias.atualizar(id, new EstrategiaRequest(e.titulo(), e.descricaoRevisada(), e.categoria(),
                        e.campanha(), null, e.metas(), hoje.plusDays(e.inicioEmDias()), hoje.plusDays(e.fimEmDias())),
                        lideranca);
            }
            if (e.retrodatarEmDias() != null) {
                retrodatarCriacao(id, hoje.minusDays(e.retrodatarEmDias()));
            }
        }
        return ids;
    }

    private Map<String, String> criarIdeias(Map<String, UsuarioLogado> pessoas, UsuarioLogado gestor,
            Map<String, String> idsDasEstrategias) {
        Map<String, String> ids = new HashMap<>();
        for (IdeiaDemo i : RoteiroDemo.ideias()) {
            String estrategiaId = i.estrategia() == null ? null : idsDasEstrategias.get(i.estrategia());
            String id = ideias.criar(new IdeiaRequest(i.titulo(), i.descricao(), i.categoria(), estrategiaId),
                    pessoas.get(i.autor())).id();
            ids.put(i.chave(), id);
            if (i.decisao() != StatusIdeia.EM_ANALISE) {
                ideias.avaliar(id, new AvaliacaoIdeiaRequest(i.decisao(), i.prioridade(), i.parecer()), gestor);
            }
        }
        return ids;
    }

    private void criarProjetos(UsuarioLogado gestor, LocalDate hoje, Map<String, String> idsDasEstrategias,
            Map<String, String> idsDasIdeias) {
        for (ProjetoDemo p : RoteiroDemo.projetos()) {
            String id = projetos.criar(new ProjetoRequest(p.nome(), p.descricao(), new BigDecimal(p.investimento()),
                    hoje.plusDays(p.inicioEmDias()), hoje.plusDays(p.prazoEmDias()),
                    idsDasEstrategias.get(p.estrategia()), idsDasIdeias.get(p.ideia()), p.status(), p.etapa()),
                    gestor).id();
            if (p.progresso() != null) {
                projetos.atualizarProgresso(id, new ProgressoRequest(null, null, p.progresso()));
            }
            for (ResultadoDemo r : p.resultados()) {
                projetos.adicionarResultado(id, new ResultadoRequest(hoje.minusDays(r.diasAtras()), r.descricao(),
                        new BigDecimal(r.retorno()), r.ganhoProdutividade(), r.reducaoCustos()), gestor);
            }
        }
    }

    /** Faz a estrategia constar como criada em outra data (a saude trata estrategia recem-criada com carencia). */
    private void retrodatarCriacao(String estrategiaId, LocalDate data) {
        mongo.updateFirst(Query.query(Criteria.where("id").is(estrategiaId)),
                Update.update("criadoEm", data.atStartOfDay(ClockConfig.FUSO).toInstant()), Estrategia.class);
    }

}
