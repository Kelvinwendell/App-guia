package br.com.fiap.aguiainova.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import br.com.fiap.aguiainova.config.AppProperties;
import br.com.fiap.aguiainova.dto.response.DesempenhoEstrategiaResponse;
import br.com.fiap.aguiainova.dto.response.DesempenhoProjetoResponse;
import br.com.fiap.aguiainova.dto.response.EvolucaoMensalResponse;
import br.com.fiap.aguiainova.dto.response.PrazoProjetoResponse;
import br.com.fiap.aguiainova.dto.response.ResumoDashboardResponse;
import br.com.fiap.aguiainova.dto.response.ResumoIdeiasResponse;
import br.com.fiap.aguiainova.model.Estrategia;
import br.com.fiap.aguiainova.model.Ideia;
import br.com.fiap.aguiainova.model.Projeto;
import br.com.fiap.aguiainova.model.ProjetoMetricas;
import br.com.fiap.aguiainova.model.enums.EtapaProjeto;
import br.com.fiap.aguiainova.model.enums.StatusIdeia;
import br.com.fiap.aguiainova.model.enums.StatusProjeto;
import br.com.fiap.aguiainova.util.Consultas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

/** Relatorios resumidos para a lideranca: alimentam os graficos e cartoes do dashboard. */
@Service
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);
    private static final int MAXIMO_PRAZOS_PROXIMOS = 5;
    private static final int CASAS_DECIMAIS = 2;
    private static final BigDecimal CEM = BigDecimal.valueOf(100);
    /** Chave de agrupamento (nao aceita nulo) para itens sem estrategia vinculada. */
    private static final String SEM_ESTRATEGIA = "";
    private static final String TITULO_SEM_ESTRATEGIA = "Sem estratégia vinculada";

    private final MongoTemplate mongo;
    private final Clock clock;
    private final int limiteRegistrosAgregacao;

    public DashboardService(MongoTemplate mongo, Clock clock, AppProperties props) {
        this.mongo = mongo;
        this.clock = clock;
        this.limiteRegistrosAgregacao = props.dashboard().limiteRegistrosAgregacao();
    }

    /**
     * Le no maximo o limite configurado de documentos da colecao, para o dashboard nunca carregar uma colecao
     * inteira na memoria. Acima do limite os totais deixam de refletir 100% dos dados - avisa no log para isso
     * nao passar despercebido enquanto o volume real ainda nao justifica reescrever como agregacao no banco.
     */
    private <T> List<T> todosComLimiteDeSeguranca(Class<T> tipo, String colecao) {
        List<T> lista = mongo.find(new Query().limit(limiteRegistrosAgregacao), tipo);
        if (lista.size() >= limiteRegistrosAgregacao) {
            log.warn("Dashboard: coleção '{}' tem {} ou mais documentos: leitura truncada em {} "
                    + "(app.dashboard.limite-registros-agregacao). Os totais não refletem 100% dos dados.",
                    colecao, limiteRegistrosAgregacao, limiteRegistrosAgregacao);
        }
        return lista;
    }

    public ResumoDashboardResponse resumo(String estrategiaId, String projetoId) {
        List<Projeto> escopo = projetosDoEscopo(estrategiaId, projetoId);
        List<Projeto> considerados = semCancelados(escopo);
        List<ProjetoMetricas> metricas = considerados.stream().map(ProjetoMetricas::de).toList();
        Totais totais = Totais.de(considerados);
        LocalDate hoje = LocalDate.now(clock);
        return new ResumoDashboardResponse(
                dinheiro(totais.investimento()), dinheiro(totais.retorno()), dinheiro(totais.lucro()), totais.roi(),
                ProjetoMetricas.mediaArredondada(metricas, ProjetoMetricas::ganhoProdutividade),
                ProjetoMetricas.mediaArredondada(metricas, ProjetoMetricas::reducaoCustos),
                escopo.size(), (int) escopo.stream().filter(p -> p.estaAtrasado(hoje)).count(),
                contarPor(escopo, Projeto::getStatus, StatusProjeto.class),
                contarPor(escopo, Projeto::getEtapa, EtapaProjeto.class),
                resumoIdeias(estrategiaId), proximosPrazos(escopo, hoje));
    }

    /** Uma linha por estrategia (mesmo sem projetos) mais, se houver, o grupo "sem estrategia vinculada". */
    public List<DesempenhoEstrategiaResponse> porEstrategia() {
        Map<String, List<Projeto>> projetosPorEstrategia = semCancelados(
                todosComLimiteDeSeguranca(Projeto.class, "projetos")).stream()
                .collect(Collectors.groupingBy(p -> chaveDe(p.getEstrategiaId())));
        Map<String, Long> ideiasPorEstrategia = todosComLimiteDeSeguranca(Ideia.class, "ideias").stream()
                .collect(Collectors.groupingBy(i -> chaveDe(i.getEstrategiaId()), Collectors.counting()));

        List<DesempenhoEstrategiaResponse> desempenhos = new ArrayList<>();
        for (Estrategia estrategia : todosComLimiteDeSeguranca(Estrategia.class, "estrategias")) {
            String chave = estrategia.getId();
            desempenhos.add(desempenho(chave, estrategia.getTitulo(), estrategia.getCategoria(),
                    projetosPorEstrategia.getOrDefault(chave, List.of()), ideiasPorEstrategia.getOrDefault(chave, 0L)));
        }
        if (projetosPorEstrategia.containsKey(SEM_ESTRATEGIA) || ideiasPorEstrategia.containsKey(SEM_ESTRATEGIA)) {
            desempenhos.add(desempenho(null, TITULO_SEM_ESTRATEGIA, null,
                    projetosPorEstrategia.getOrDefault(SEM_ESTRATEGIA, List.of()),
                    ideiasPorEstrategia.getOrDefault(SEM_ESTRATEGIA, 0L)));
        }
        desempenhos.sort(Comparator.comparing(DesempenhoEstrategiaResponse::lucro, Comparator.reverseOrder()));
        return desempenhos;
    }

    public List<DesempenhoProjetoResponse> porProjeto(String estrategiaId) {
        LocalDate hoje = LocalDate.now(clock);
        return projetosDoEscopo(estrategiaId, null).stream()
                .map(projeto -> desempenhoDoProjeto(projeto, hoje))
                .sorted(Comparator.comparing(DesempenhoProjetoResponse::lucro, Comparator.reverseOrder()))
                .toList();
    }

    /** Retorno gerado por mes (e acumulado), com todos os meses entre o primeiro e o ultimo resultado. */
    public List<EvolucaoMensalResponse> evolucaoMensal(String estrategiaId, String projetoId) {
        NavigableMap<YearMonth, BigDecimal> retornoPorMes = new TreeMap<>();
        semCancelados(projetosDoEscopo(estrategiaId, projetoId)).stream()
                .flatMap(projeto -> projeto.getResultados().stream())
                .forEach(resultado -> retornoPorMes.merge(YearMonth.from(resultado.data()),
                        resultado.retornoFinanceiro(), BigDecimal::add));
        return retornoPorMes.isEmpty() ? List.of() : serieContinua(retornoPorMes);
    }

    private static List<EvolucaoMensalResponse> serieContinua(NavigableMap<YearMonth, BigDecimal> retornoPorMes) {
        List<EvolucaoMensalResponse> serie = new ArrayList<>();
        BigDecimal acumulado = BigDecimal.ZERO;
        for (YearMonth mes = retornoPorMes.firstKey(); !mes.isAfter(retornoPorMes.lastKey()); mes = mes.plusMonths(1)) {
            BigDecimal noMes = retornoPorMes.getOrDefault(mes, BigDecimal.ZERO);
            acumulado = acumulado.add(noMes);
            serie.add(new EvolucaoMensalResponse(mes.toString(), dinheiro(noMes), dinheiro(acumulado)));
        }
        return serie;
    }

    private List<Projeto> projetosDoEscopo(String estrategiaId, String projetoId) {
        Query consulta = new Query();
        if (Consultas.informado(estrategiaId)) {
            consulta.addCriteria(Criteria.where("estrategiaId").is(estrategiaId));
        }
        if (Consultas.informado(projetoId)) {
            consulta.addCriteria(Criteria.where("id").is(projetoId));
        }
        return mongo.find(consulta, Projeto.class);
    }

    private ResumoIdeiasResponse resumoIdeias(String estrategiaId) {
        List<Ideia> lista = Consultas.informado(estrategiaId)
                ? mongo.find(new Query(Criteria.where("estrategiaId").is(estrategiaId)), Ideia.class)
                : todosComLimiteDeSeguranca(Ideia.class, "ideias");
        Map<StatusIdeia, Long> porStatus = lista.stream()
                .collect(Collectors.groupingBy(Ideia::getStatus, Collectors.counting()));
        long aprovadas = porStatus.getOrDefault(StatusIdeia.APROVADA, 0L);
        long rejeitadas = porStatus.getOrDefault(StatusIdeia.REJEITADA, 0L);
        long emAnalise = porStatus.getOrDefault(StatusIdeia.EM_ANALISE, 0L);
        return new ResumoIdeiasResponse(lista.size(), emAnalise, aprovadas, rejeitadas,
                percentual(aprovadas, aprovadas + rejeitadas));
    }

    private static List<PrazoProjetoResponse> proximosPrazos(List<Projeto> escopo, LocalDate hoje) {
        return escopo.stream()
                .filter(projeto -> projeto.getStatus().emAberto())
                .sorted(Comparator.comparing(Projeto::getPrazo))
                .limit(MAXIMO_PRAZOS_PROXIMOS)
                .map(projeto -> new PrazoProjetoResponse(projeto.getId(), projeto.getNome(), projeto.getStatus(),
                        projeto.getProgresso(), projeto.getPrazo(), projeto.diasParaPrazo(hoje),
                        projeto.estaAtrasado(hoje)))
                .toList();
    }

    private static DesempenhoEstrategiaResponse desempenho(String estrategiaId, String titulo, String categoria,
            List<Projeto> projetosDaEstrategia, long quantidadeIdeias) {
        Totais totais = Totais.de(projetosDaEstrategia);
        return new DesempenhoEstrategiaResponse(estrategiaId, titulo, categoria, projetosDaEstrategia.size(),
                quantidadeIdeias, dinheiro(totais.investimento()), dinheiro(totais.retorno()),
                dinheiro(totais.lucro()), totais.roi());
    }

    private static DesempenhoProjetoResponse desempenhoDoProjeto(Projeto projeto, LocalDate hoje) {
        ProjetoMetricas metricas = ProjetoMetricas.de(projeto);
        return new DesempenhoProjetoResponse(projeto.getId(), projeto.getNome(), projeto.getStatus(),
                projeto.getEtapa(), projeto.getProgresso(), projeto.getPrazo(), projeto.estaAtrasado(hoje),
                dinheiro(projeto.getInvestimento()), dinheiro(metricas.retornoFinanceiro()),
                dinheiro(metricas.lucro()), metricas.roi(), metricas.ganhoProdutividade(), metricas.reducaoCustos());
    }

    private static List<Projeto> semCancelados(List<Projeto> lista) {
        return lista.stream().filter(projeto -> projeto.getStatus() != StatusProjeto.CANCELADO).toList();
    }

    /** Contagem por valor do enum, incluindo zeros, para que os graficos tenham sempre todas as categorias. */
    private static <E extends Enum<E>> Map<E, Long> contarPor(List<Projeto> lista, Function<Projeto, E> chave,
            Class<E> tipo) {
        Map<E, Long> contagem = new EnumMap<>(tipo);
        for (E valor : tipo.getEnumConstants()) {
            contagem.put(valor, 0L);
        }
        lista.forEach(projeto -> contagem.merge(chave.apply(projeto), 1L, Long::sum));
        return contagem;
    }

    private static String chaveDe(String estrategiaId) {
        return Consultas.informado(estrategiaId) ? estrategiaId : SEM_ESTRATEGIA;
    }

    private static BigDecimal dinheiro(BigDecimal valor) {
        return valor.setScale(CASAS_DECIMAIS, RoundingMode.HALF_UP);
    }

    private static BigDecimal percentual(long parte, long total) {
        if (total == 0) {
            return null;
        }
        return BigDecimal.valueOf(parte).multiply(CEM)
                .divide(BigDecimal.valueOf(total), CASAS_DECIMAIS, RoundingMode.HALF_UP);
    }

    /** Soma de investimento e retorno de um conjunto de projetos, com lucro e ROI derivados. */
    private record Totais(BigDecimal investimento, BigDecimal retorno) {

        static Totais de(List<Projeto> projetos) {
            BigDecimal investimento = BigDecimal.ZERO;
            BigDecimal retorno = BigDecimal.ZERO;
            for (Projeto projeto : projetos) {
                investimento = investimento.add(projeto.getInvestimento());
                retorno = retorno.add(ProjetoMetricas.de(projeto).retornoFinanceiro());
            }
            return new Totais(investimento, retorno);
        }

        BigDecimal lucro() {
            return retorno.subtract(investimento);
        }

        BigDecimal roi() {
            return ProjetoMetricas.roi(lucro(), investimento);
        }
    }
}
