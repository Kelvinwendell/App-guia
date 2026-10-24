package br.com.fiap.aguiainova.service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import br.com.fiap.aguiainova.config.AppProperties;
import br.com.fiap.aguiainova.dto.request.AnaliseEstrategiaRequest;
import br.com.fiap.aguiainova.dto.request.RascunhoEstrategiaRequest;
import br.com.fiap.aguiainova.dto.response.AnaliseEstrategiaResponse;
import br.com.fiap.aguiainova.dto.response.LeituraSaudeResponse;
import br.com.fiap.aguiainova.dto.response.RascunhoEstrategiaResponse;
import br.com.fiap.aguiainova.exception.LimiteDeRequisicoesExcedidoException;
import br.com.fiap.aguiainova.exception.ServicoIndisponivelException;
import br.com.fiap.aguiainova.integration.ai.LlmClient;
import br.com.fiap.aguiainova.integration.ai.LlmIndisponivelException;
import br.com.fiap.aguiainova.integration.ai.LlmNaoConfiguradoException;
import br.com.fiap.aguiainova.mapper.EstrategiaMapper;
import br.com.fiap.aguiainova.model.DadosEstrategia;
import br.com.fiap.aguiainova.model.DiagnosticoEstrategia;
import br.com.fiap.aguiainova.model.Estrategia;
import br.com.fiap.aguiainova.model.enums.StatusEstrategia;
import br.com.fiap.aguiainova.repository.EstrategiaRepository;
import br.com.fiap.aguiainova.security.LimitadorDeTaxa;
import br.com.fiap.aguiainova.security.UsuarioLogado;
import br.com.fiap.aguiainova.service.ia.AnaliseGerada;
import br.com.fiap.aguiainova.service.ia.AnalisadorDeEstrategias;
import br.com.fiap.aguiainova.service.ia.InterpretadorRespostaIa;
import br.com.fiap.aguiainova.service.ia.LeituraGerada;
import br.com.fiap.aguiainova.service.ia.LeitorDeSaude;
import br.com.fiap.aguiainova.service.ia.PromptsIa;
import br.com.fiap.aguiainova.service.ia.RascunhoGerado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * IA aplicada as estrategias: rascunho a partir de um objetivo, critica antes de publicar e leitura da saude.
 * A IA so sugere; salvar, encerrar ou mudar uma estrategia continua sendo da lideranca. O rascunho nao tem substituto
 * por regras (como o chat): sem IA disponivel, responde 503. Critica e leitura caem nas regras. Como as demais
 * chamadas de IA, as tres operacoes dividem o limite por minuto de cada usuario (o mesmo balde do chat e dos insights).
 */
@Service
public class EstrategiaIaService {

    private static final Logger log = LoggerFactory.getLogger(EstrategiaIaService.class);
    private static final String MENSAGEM_LIMITE_EXCEDIDO =
            "Muitas chamadas de IA em pouco tempo. Tente novamente em instantes.";

    private final LlmClient llm;
    private final PromptsIa prompts;
    private final InterpretadorRespostaIa interpretador;
    private final AnalisadorDeEstrategias analisador;
    private final LeitorDeSaude leitor;
    private final EstrategiaRepository estrategias;
    private final EstrategiaSaudeService saude;
    private final EstrategiaMapper mapper;
    private final Clock clock;
    private final LimitadorDeTaxa limitador;
    private final int limiteIaPorMinuto;

    public EstrategiaIaService(LlmClient llm, PromptsIa prompts, InterpretadorRespostaIa interpretador,
            AnalisadorDeEstrategias analisador, LeitorDeSaude leitor, EstrategiaRepository estrategias,
            EstrategiaSaudeService saude, EstrategiaMapper mapper, Clock clock, LimitadorDeTaxa limitador,
            AppProperties props) {
        this.llm = llm;
        this.prompts = prompts;
        this.interpretador = interpretador;
        this.analisador = analisador;
        this.leitor = leitor;
        this.estrategias = estrategias;
        this.saude = saude;
        this.mapper = mapper;
        this.clock = clock;
        this.limitador = limitador;
        this.limiteIaPorMinuto = props.rateLimit().iaPorMinuto();
    }

    public RascunhoEstrategiaResponse rascunhar(RascunhoEstrategiaRequest req, UsuarioLogado usuario) {
        exigirLimite(usuario);
        try {
            String resposta = llm.gerar(prompts.rascunhoDeEstrategia(req.objetivo(),
                    estrategias.findByStatus(StatusEstrategia.VIGENTE), LocalDate.now(clock)));
            RascunhoGerado rascunho = interpretador.interpretarRascunho(resposta, llm.provedor());
            return new RascunhoEstrategiaResponse(rascunho.titulo(), rascunho.descricao(), rascunho.categoria(),
                    rascunho.campanha(), rascunho.metas(), rascunho.inicio(), rascunho.fim(), rascunho.fonte());
        } catch (LlmNaoConfiguradoException e) {
            throw new ServicoIndisponivelException("A IA não está configurada neste ambiente.");
        } catch (LlmIndisponivelException e) {
            log.warn("IA indisponível para rascunhar a estratégia. Motivo: {}", e.getMessage());
            throw new ServicoIndisponivelException(
                    "A IA está indisponível no momento. Tente novamente em instantes.");
        }
    }

    public AnaliseEstrategiaResponse analisar(AnaliseEstrategiaRequest req, UsuarioLogado usuario) {
        exigirLimite(usuario);
        DadosEstrategia dados = mapper.paraDados(req.estrategia());
        List<Estrategia> outras = estrategias.findByStatus(StatusEstrategia.VIGENTE).stream()
                .filter(estrategia -> !estrategia.getId().equals(req.estrategiaId()))
                .toList();
        AnaliseGerada analise = analisador.analisar(dados, outras);
        Map<String, String> titulos = outras.stream().collect(Collectors.toMap(Estrategia::getId,
                Estrategia::getTitulo, (primeiro, segundo) -> primeiro));
        List<AnaliseEstrategiaResponse.Sobreposicao> sobreposicoes = analise.sobreposicoes().stream()
                .map(id -> new AnaliseEstrategiaResponse.Sobreposicao(id, titulos.get(id)))
                .toList();
        return new AnaliseEstrategiaResponse(analise.nota(), analise.resumo(), analise.pontosFortes(),
                analise.melhorias(), sobreposicoes, analise.fonte(), clock.instant());
    }

    public LeituraSaudeResponse lerSaude(String estrategiaId, UsuarioLogado usuario) {
        exigirLimite(usuario);
        DiagnosticoEstrategia diagnostico = saude.diagnosticar(estrategiaId);
        LeituraGerada leitura = leitor.ler(diagnostico);
        return new LeituraSaudeResponse(mapper.paraResponse(diagnostico), leitura.leitura(), leitura.recomendacoes(),
                leitura.fonte(), clock.instant());
    }

    /** Conta a chamada antes de executa-la, qualquer que seja o resultado: o limite protege o custo do provedor. */
    private void exigirLimite(UsuarioLogado usuario) {
        if (!limitador.permitir("ia:" + usuario.id(), limiteIaPorMinuto, Duration.ofMinutes(1))) {
            throw new LimiteDeRequisicoesExcedidoException(MENSAGEM_LIMITE_EXCEDIDO);
        }
    }
}
