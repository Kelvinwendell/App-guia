package br.com.fiap.aguiainova.bootstrap;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import br.com.fiap.aguiainova.dto.request.AvaliacaoIdeiaRequest;
import br.com.fiap.aguiainova.dto.request.EstrategiaRequest;
import br.com.fiap.aguiainova.dto.request.IdeiaRequest;
import br.com.fiap.aguiainova.dto.request.ProgressoRequest;
import br.com.fiap.aguiainova.dto.request.ProjetoRequest;
import br.com.fiap.aguiainova.dto.request.ResultadoRequest;
import br.com.fiap.aguiainova.model.DadosUsuario;
import br.com.fiap.aguiainova.model.Usuario;
import br.com.fiap.aguiainova.model.enums.EtapaProjeto;
import br.com.fiap.aguiainova.model.enums.Perfil;
import br.com.fiap.aguiainova.model.enums.Prioridade;
import br.com.fiap.aguiainova.model.enums.StatusEstrategia;
import br.com.fiap.aguiainova.model.enums.StatusIdeia;
import br.com.fiap.aguiainova.model.enums.StatusProjeto;
import br.com.fiap.aguiainova.repository.UsuarioRepository;
import br.com.fiap.aguiainova.security.UsuarioLogado;
import br.com.fiap.aguiainova.service.EstrategiaService;
import br.com.fiap.aguiainova.service.IdeiaService;
import br.com.fiap.aguiainova.service.ProjetoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Popula um banco vazio com os usuarios de teste da documentacao do app (um por perfil) e dados de exemplo, para
 * que o dashboard e as telas tenham o que mostrar. Tudo e criado pelos mesmos servicos usados pela API, entao as
 * regras de negocio e o historico de estrategias ficam consistentes. Desative com {@code APP_SEED_ENABLED=false}.
 */
@Component
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final String SENHA_DOS_USUARIOS_DE_TESTE = "123456";

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final EstrategiaService estrategias;
    private final IdeiaService ideias;
    private final ProjetoService projetos;
    private final Clock clock;

    public DataSeeder(UsuarioRepository usuarios, PasswordEncoder encoder, EstrategiaService estrategias,
            IdeiaService ideias, ProjetoService projetos, Clock clock) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.estrategias = estrategias;
        this.ideias = ideias;
        this.projetos = projetos;
        this.clock = clock;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (usuarios.count() > 0) {
            log.info("O banco já possui usuários; dados de exemplo não serão criados.");
            return;
        }
        log.warn("Criando usuários e dados de exemplo (app.seed.enabled=true). "
                + "Em produção, desative com APP_SEED_ENABLED=false.");
        UsuarioLogado operador = criarUsuario("Vinícius", "vinicius@gmail.com", Perfil.OPERADOR,
                "Operador de Produção", "Produção", "Unidade São Paulo");
        UsuarioLogado gestor = criarUsuario("Anna", "anna@gmail.com", Perfil.GESTOR,
                "Gerente de Inovação", "Inovação", "Unidade São Paulo");
        UsuarioLogado lideranca = criarUsuario("Gabriela", "gabriela@gmail.com", Perfil.LIDERANCA,
                "Diretora de Operações", "Diretoria", "Matriz");

        Estrategias estrategiasCriadas = criarEstrategias(lideranca);
        Ideias ideiasCriadas = criarIdeias(operador, gestor, estrategiasCriadas);
        criarProjetos(gestor, estrategiasCriadas, ideiasCriadas);
    }

    private UsuarioLogado criarUsuario(String nome, String email, Perfil perfil, String cargo, String departamento,
            String unidade) {
        var dados = new DadosUsuario(nome, email, encoder.encode(SENHA_DOS_USUARIOS_DE_TESTE), perfil, cargo,
                departamento, unidade);
        Usuario salvo = usuarios.save(new Usuario(dados));
        return new UsuarioLogado(salvo.getId(), salvo.getNome(), salvo.getEmail(), salvo.getPerfil());
    }

    private Estrategias criarEstrategias(UsuarioLogado lideranca) {
        LocalDate hoje = LocalDate.now(clock);
        String zeroDesperdicio = estrategias.criar(new EstrategiaRequest("Redução de desperdício na produção",
                "Reduzir perdas de material e retrabalho nas linhas de produção.", "Sustentabilidade",
                "Zero Desperdício 2026", null,
                List.of("Reduzir o desperdício de material em 15% até o fim do período",
                        "Reaproveitar pelo menos 30% das sobras de corte"),
                hoje.minusMonths(8), hoje.plusMonths(4)), lideranca).id();
        String fabrica40 = estrategias.criar(new EstrategiaRequest("Transformação digital do chão de fábrica",
                "Digitalizar processos e monitorar máquinas em tempo real.", "Tecnologia", "Fábrica 4.0", null,
                List.of("Monitorar 100% das máquinas da linha 3 em tempo real", "Reduzir paradas não planejadas em 20%"),
                hoje.minusMonths(6), hoje.plusMonths(6)), lideranca).id();
        estrategias.atualizar(fabrica40, new EstrategiaRequest("Transformação digital do chão de fábrica",
                "Digitalizar processos, monitorar máquinas em tempo real e usar dados para decidir.", "Tecnologia",
                "Fábrica 4.0", null,
                List.of("Monitorar 100% das máquinas da linha 3 em tempo real", "Reduzir paradas não planejadas em 20%"),
                hoje.minusMonths(6), hoje.plusMonths(6)), lideranca);
        String cuidarSempre = estrategias.criar(new EstrategiaRequest("Segurança e bem-estar do colaborador",
                "Reduzir acidentes e afastamentos com ergonomia e prevenção.", "Pessoas", "Cuidar Sempre", null,
                List.of("Reduzir os afastamentos por lesão em 25% em 12 meses"),
                hoje.minusMonths(6), hoje.plusMonths(6)), lideranca).id();
        estrategias.criar(new EstrategiaRequest("Expansão dos canais de venda online",
                "Ampliar as vendas por canais digitais.", "Comercial", "E-commerce 2025",
                StatusEstrategia.ENCERRADA), lideranca);
        return new Estrategias(zeroDesperdicio, fabrica40, cuidarSempre);
    }

    private Ideias criarIdeias(UsuarioLogado operador, UsuarioLogado gestor, Estrategias estrategiasCriadas) {
        String sensores = criarIdeiaAprovada(operador, gestor, "Sensores de vibração nas máquinas da linha 3",
                "Instalar sensores IoT de vibração e temperatura para prever falhas e reduzir paradas não "
                        + "planejadas, enviando os dados para um painel em tempo real.",
                "Tecnologia", estrategiasCriadas.fabrica40(), Prioridade.ALTA);
        String sobras = criarIdeiaAprovada(operador, gestor, "Reaproveitar sobras de material na produção",
                "Separar as sobras de corte e reutilizá-las na fabricação de peças menores, diminuindo a compra "
                        + "de matéria-prima e o descarte.",
                "Sustentabilidade", estrategiasCriadas.zeroDesperdicio(), Prioridade.MEDIA);
        String rodizio = criarIdeiaAprovada(operador, gestor, "Rodízio de tarefas para reduzir lesões",
                "Alternar as tarefas repetitivas entre os operadores a cada duas horas para diminuir lesões por "
                        + "esforço repetitivo.",
                "Pessoas", estrategiasCriadas.cuidarSempre(), Prioridade.MEDIA);

        ideias.criar(new IdeiaRequest("Checklist digital de início de turno",
                "Trocar o checklist em papel por um formulário no celular, com fotos e alertas automáticos para "
                        + "a manutenção.", "Tecnologia", estrategiasCriadas.fabrica40()), operador);
        ideias.criar(new IdeiaRequest("Descarte seletivo nas estações de trabalho",
                "Instalar lixeiras de coleta seletiva em cada estação para aumentar a reciclagem de resíduos.",
                "Sustentabilidade", estrategiasCriadas.zeroDesperdicio()), operador);

        String rejeitada = ideias.criar(new IdeiaRequest("Máquina de café premium no refeitório",
                "Comprar uma máquina de café expresso para o refeitório.", "Outros", null), operador).id();
        ideias.avaliar(rejeitada, new AvaliacaoIdeiaRequest(StatusIdeia.REJEITADA, Prioridade.BAIXA,
                "Fora do escopo das estratégias vigentes."), gestor);
        return new Ideias(sensores, sobras, rodizio);
    }

    private String criarIdeiaAprovada(UsuarioLogado operador, UsuarioLogado gestor, String titulo, String descricao,
            String categoria, String estrategiaId, Prioridade prioridade) {
        String id = ideias.criar(new IdeiaRequest(titulo, descricao, categoria, estrategiaId), operador).id();
        ideias.avaliar(id, new AvaliacaoIdeiaRequest(StatusIdeia.APROVADA, prioridade,
                "Aprovada para virar projeto."), gestor);
        return id;
    }

    private void criarProjetos(UsuarioLogado gestor, Estrategias estrategiasCriadas, Ideias ideiasCriadas) {
        LocalDate hoje = LocalDate.now(clock);

        String sensores = criarProjeto(gestor, "Sensores IoT na linha 3",
                "Implantação de sensores de vibração e painel de monitoramento na linha 3.", "120000",
                hoje.minusDays(150), hoje.plusDays(90), estrategiasCriadas.fabrica40(), ideiasCriadas.sensores(),
                StatusProjeto.EM_ANDAMENTO, EtapaProjeto.EXECUCAO);
        projetos.atualizarProgresso(sensores, new ProgressoRequest(null, null, 55));
        registrarResultado(sensores, gestor, hoje.minusDays(120), "Redução de paradas no primeiro trimestre", "15000", 4.0, 2.0);
        registrarResultado(sensores, gestor, hoje.minusDays(90), "Menos manutenção corretiva", "22000", 6.0, 3.0);
        registrarResultado(sensores, gestor, hoje.minusDays(60), "Previsão de falhas em produção", "31000", 9.0, 5.0);
        registrarResultado(sensores, gestor, hoje.minusDays(30), "Ganho de disponibilidade das máquinas", "28000", 10.0, 6.0);

        String sobras = criarProjeto(gestor, "Reaproveitamento de sobras de material",
                "Linha de peças pequenas fabricadas a partir das sobras de corte.", "60000",
                hoje.minusDays(240), hoje.minusDays(30), estrategiasCriadas.zeroDesperdicio(), ideiasCriadas.sobras(),
                StatusProjeto.CONCLUIDO, EtapaProjeto.ENCERRAMENTO);
        registrarResultado(sobras, gestor, hoje.minusDays(200), "Economia de matéria-prima", "60000", 3.0, 8.0);
        registrarResultado(sobras, gestor, hoje.minusDays(150), "Venda das peças reaproveitadas", "80000", 4.0, 10.0);
        registrarResultado(sobras, gestor, hoje.minusDays(100), "Redução do descarte", "50000", null, 12.0);

        String ergonomia = criarProjeto(gestor, "Programa de ergonomia nas estações",
                "Adequação das estações de trabalho e rodízio de tarefas.", "45000",
                hoje.minusDays(180), hoje.minusDays(10), estrategiasCriadas.cuidarSempre(), ideiasCriadas.rodizio(),
                StatusProjeto.EM_ANDAMENTO, EtapaProjeto.MONITORAMENTO);
        projetos.atualizarProgresso(ergonomia, new ProgressoRequest(null, null, 80));
        registrarResultado(ergonomia, gestor, hoje.minusDays(60), "Queda nos afastamentos por lesão", "12000", 5.0, null);

        criarProjeto(gestor, "Painel de indicadores em tempo real",
                "Painel único com os indicadores da fábrica.", "30000", hoje, hoje.plusDays(150),
                estrategiasCriadas.fabrica40(), null, StatusProjeto.PLANEJAMENTO, EtapaProjeto.INICIACAO);
    }

    private String criarProjeto(UsuarioLogado gestor, String nome, String descricao, String investimento,
            LocalDate inicio, LocalDate prazo, String estrategiaId, String ideiaId, StatusProjeto status,
            EtapaProjeto etapa) {
        return projetos.criar(new ProjetoRequest(nome, descricao, new BigDecimal(investimento), inicio, prazo,
                estrategiaId, ideiaId, status, etapa), gestor).id();
    }

    private void registrarResultado(String projetoId, UsuarioLogado gestor, LocalDate data, String descricao,
            String retorno, Double produtividade, Double reducaoCustos) {
        projetos.adicionarResultado(projetoId, new ResultadoRequest(data, descricao, new BigDecimal(retorno),
                produtividade, reducaoCustos), gestor);
    }

    private record Estrategias(String zeroDesperdicio, String fabrica40, String cuidarSempre) {
    }

    private record Ideias(String sensores, String sobras, String rodizio) {
    }
}
