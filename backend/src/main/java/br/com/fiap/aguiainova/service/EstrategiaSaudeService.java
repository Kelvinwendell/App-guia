package br.com.fiap.aguiainova.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import br.com.fiap.aguiainova.config.ClockConfig;
import br.com.fiap.aguiainova.dto.response.SaudeEstrategiaResponse;
import br.com.fiap.aguiainova.exception.NaoEncontradoException;
import br.com.fiap.aguiainova.exception.RegraDeNegocioException;
import br.com.fiap.aguiainova.mapper.EstrategiaMapper;
import br.com.fiap.aguiainova.model.DiagnosticoEstrategia;
import br.com.fiap.aguiainova.model.Estrategia;
import br.com.fiap.aguiainova.model.Ideia;
import br.com.fiap.aguiainova.model.IndicadoresEstrategia;
import br.com.fiap.aguiainova.model.Projeto;
import br.com.fiap.aguiainova.model.ProjetoMetricas;
import br.com.fiap.aguiainova.model.ResultadoProjeto;
import br.com.fiap.aguiainova.model.SaudeEstrategia;
import br.com.fiap.aguiainova.model.enums.StatusEstrategia;
import br.com.fiap.aguiainova.model.enums.StatusIdeia;
import br.com.fiap.aguiainova.model.enums.StatusProjeto;
import br.com.fiap.aguiainova.repository.EstrategiaRepository;
import br.com.fiap.aguiainova.repository.IdeiaRepository;
import br.com.fiap.aguiainova.repository.ProjetoRepository;
import org.springframework.stereotype.Service;

/**
 * Acompanha a saude das estrategias vigentes: junta o que esta vinculado a cada uma (ideias, projetos, resultados)
 * e entrega o diagnostico calculado pelas regras de {@link SaudeEstrategia}. Consultas por conjunto de ids, entao o
 * custo nao cresce com o numero de estrategias.
 */
@Service
public class EstrategiaSaudeService {

    private static final int CASAS_DECIMAIS = 2;

    private final EstrategiaRepository estrategias;
    private final IdeiaRepository ideias;
    private final ProjetoRepository projetos;
    private final EstrategiaMapper mapper;
    private final Clock clock;

    public EstrategiaSaudeService(EstrategiaRepository estrategias, IdeiaRepository ideias,
            ProjetoRepository projetos, EstrategiaMapper mapper, Clock clock) {
        this.estrategias = estrategias;
        this.ideias = ideias;
        this.projetos = projetos;
        this.mapper = mapper;
        this.clock = clock;
    }

    public List<SaudeEstrategiaResponse> listarVigentes() {
        List<Estrategia> vigentes = estrategias.findByStatus(StatusEstrategia.VIGENTE);
        return vigentes.isEmpty() ? List.of() : diagnosticar(vigentes).stream().map(mapper::paraResponse).toList();
    }

    public SaudeEstrategiaResponse obter(String id) {
        return mapper.paraResponse(diagnosticar(id));
    }

    /** Diagnostico de uma estrategia vigente; usado tambem pela IA, que precisa dos numeros alem do texto. */
    public DiagnosticoEstrategia diagnosticar(String id) {
        Estrategia estrategia = estrategias.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Estratégia não encontrada."));
        if (!estrategia.estaVigente()) {
            throw new RegraDeNegocioException("Só estratégias vigentes têm a saúde acompanhada.");
        }
        return diagnosticar(List.of(estrategia)).get(0);
    }

    private List<DiagnosticoEstrategia> diagnosticar(List<Estrategia> lista) {
        LocalDate hoje = LocalDate.now(clock);
        List<String> ids = lista.stream().map(Estrategia::getId).toList();
        Map<String, List<Ideia>> ideiasPorEstrategia = ideias.findByEstrategiaIdIn(ids).stream()
                .collect(Collectors.groupingBy(Ideia::getEstrategiaId));
        Map<String, List<Projeto>> projetosPorEstrategia = projetos.findByEstrategiaIdIn(ids).stream()
                .collect(Collectors.groupingBy(Projeto::getEstrategiaId));
        Set<String> ideiasComProjeto = ideiasQueViraramProjeto(ideiasPorEstrategia.values());

        return lista.stream().map(estrategia -> {
            IndicadoresEstrategia indicadores = indicadoresDe(estrategia,
                    ideiasPorEstrategia.getOrDefault(estrategia.getId(), List.of()),
                    projetosPorEstrategia.getOrDefault(estrategia.getId(), List.of()), ideiasComProjeto, hoje);
            return new DiagnosticoEstrategia(estrategia, indicadores, SaudeEstrategia.avaliar(indicadores, hoje));
        }).toList();
    }

    /** Ids das ideias aprovadas que ja tem projeto (mesmo que o projeto esteja em outra estrategia ou cancelado). */
    private Set<String> ideiasQueViraramProjeto(Collection<List<Ideia>> ideiasPorEstrategia) {
        List<String> aprovadas = ideiasPorEstrategia.stream().flatMap(List::stream)
                .filter(ideia -> ideia.getStatus() == StatusIdeia.APROVADA)
                .map(Ideia::getId)
                .toList();
        if (aprovadas.isEmpty()) {
            return Set.of();
        }
        return projetos.findByIdeiaIdIn(aprovadas).stream().map(Projeto::getIdeiaId).collect(Collectors.toSet());
    }

    private static IndicadoresEstrategia indicadoresDe(Estrategia estrategia, List<Ideia> ideias,
            List<Projeto> todosOsProjetos, Set<String> ideiasComProjeto, LocalDate hoje) {
        List<Projeto> projetos = todosOsProjetos.stream()
                .filter(projeto -> projeto.getStatus() != StatusProjeto.CANCELADO)
                .toList();
        BigDecimal investimento = projetos.stream().map(Projeto::getInvestimento)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal retorno = projetos.stream().map(projeto -> ProjetoMetricas.de(projeto).retornoFinanceiro())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new IndicadoresEstrategia(
                dia(estrategia.getCriadoEm(), hoje), estrategia.getInicio(), estrategia.getFim(),
                estrategia.getMetas().size(), ideias.size(),
                ideias.stream().filter(Ideia::estaEmAnalise).count(),
                ideias.stream()
                        .filter(ideia -> ideia.getStatus() == StatusIdeia.APROVADA)
                        .filter(ideia -> !ideiasComProjeto.contains(ideia.getId()))
                        .count(),
                projetos.size(),
                (int) projetos.stream().filter(projeto -> projeto.getStatus().emAberto()).count(),
                (int) projetos.stream().filter(projeto -> projeto.estaAtrasado(hoje)).count(),
                (int) projetos.stream().filter(projeto -> projeto.getStatus() == StatusProjeto.CONCLUIDO).count(),
                dinheiro(investimento), dinheiro(retorno),
                ProjetoMetricas.roi(retorno.subtract(investimento), investimento),
                ultimaAtividade(ideias, projetos));
    }

    /**
     * Ultima movimentacao feita por pessoas: criacao e decisao das ideias, criacao e alteracao dos projetos e datas
     * dos resultados. A nota da IA nao conta: reavaliar uma ideia nao e tracao.
     */
    private static LocalDate ultimaAtividade(List<Ideia> ideias, List<Projeto> projetos) {
        Stream<Instant> instantes = Stream.concat(
                ideias.stream().flatMap(ideia -> Stream.of(ideia.getCriadoEm(), ideia.getAvaliadaEm())),
                projetos.stream().flatMap(projeto -> Stream.of(projeto.getCriadoEm(), projeto.getAtualizadoEm())));
        Stream<LocalDate> datas = Stream.concat(
                instantes.filter(Objects::nonNull).map(instante -> dia(instante, null)),
                projetos.stream().flatMap(projeto -> projeto.getResultados().stream()).map(ResultadoProjeto::data));
        return datas.filter(Objects::nonNull).max(LocalDate::compareTo).orElse(null);
    }

    private static LocalDate dia(Instant instante, LocalDate padrao) {
        return instante == null ? padrao : instante.atZone(ClockConfig.FUSO).toLocalDate();
    }

    private static BigDecimal dinheiro(BigDecimal valor) {
        return valor.setScale(CASAS_DECIMAIS, RoundingMode.HALF_UP);
    }
}
