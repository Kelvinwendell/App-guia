package br.com.fiap.aguiainova.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import br.com.fiap.aguiainova.dto.response.AvaliacaoIaResponse;
import br.com.fiap.aguiainova.dto.response.RankingIaResponse;
import br.com.fiap.aguiainova.exception.NaoEncontradoException;
import br.com.fiap.aguiainova.model.AvaliacaoIa;
import br.com.fiap.aguiainova.model.Estrategia;
import br.com.fiap.aguiainova.model.Ideia;
import br.com.fiap.aguiainova.model.enums.StatusEstrategia;
import br.com.fiap.aguiainova.model.enums.StatusIdeia;
import br.com.fiap.aguiainova.repository.EstrategiaRepository;
import br.com.fiap.aguiainova.repository.IdeiaRepository;
import br.com.fiap.aguiainova.service.ia.AvaliadorDeIdeias;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * Pontuacao e priorizacao de ideias. A nota e apenas uma sugestao ao gestor. De onde ela vem (IA generativa ou
 * regras de contingencia) e decidido pelo {@link AvaliadorDeIdeias} injetado; aqui so se orquestra e persiste.
 */
@Service
public class AvaliacaoIaService {

    /** Limita o tamanho do prompt e o consumo da cota gratuita: as demais ficam para a proxima chamada. */
    private static final int MAXIMO_IDEIAS_POR_LOTE = 20;
    private static final Sort MAIS_RECENTES = Sort.by(Sort.Direction.DESC, "criadoEm");

    private final AvaliadorDeIdeias avaliador;
    private final IdeiaRepository ideias;
    private final EstrategiaRepository estrategias;

    public AvaliacaoIaService(AvaliadorDeIdeias avaliador, IdeiaRepository ideias, EstrategiaRepository estrategias) {
        this.avaliador = avaliador;
        this.ideias = ideias;
        this.estrategias = estrategias;
    }

    /** Pontua (ou repontua) uma ideia especifica. */
    public AvaliacaoIaResponse avaliar(String ideiaId) {
        Ideia ideia = ideias.findById(ideiaId).orElseThrow(() -> new NaoEncontradoException("Ideia não encontrada."));
        pontuarEPersistir(List.of(ideia));
        return paraResponse(ideia);
    }

    /** Pontua as ideias EM_ANALISE ainda sem nota, numa unica rodada, e devolve o ranking. */
    public RankingIaResponse avaliarPendentes() {
        List<Ideia> emAnalise = ideias.findByStatus(StatusIdeia.EM_ANALISE, MAIS_RECENTES);
        List<Ideia> semNota = emAnalise.stream()
                .filter(ideia -> ideia.getAvaliacaoIa() == null)
                .limit(MAXIMO_IDEIAS_POR_LOTE)
                .toList();
        if (!semNota.isEmpty()) {
            pontuarEPersistir(semNota);
        }
        List<AvaliacaoIaResponse> ranking = emAnalise.stream()
                .filter(ideia -> ideia.getAvaliacaoIa() != null)
                .sorted(Comparator.comparingInt((Ideia ideia) -> ideia.getAvaliacaoIa().score()).reversed())
                .map(AvaliacaoIaService::paraResponse)
                .toList();
        return new RankingIaResponse(ranking, semNota.size());
    }

    private void pontuarEPersistir(List<Ideia> lote) {
        List<Estrategia> vigentes = estrategias.findByStatus(StatusEstrategia.VIGENTE);
        Map<String, AvaliacaoIa> avaliacoes = avaliador.avaliar(lote, vigentes);
        for (Ideia ideia : lote) {
            AvaliacaoIa avaliacao = avaliacoes.get(ideia.getId());
            if (avaliacao != null) {
                ideia.registrarAvaliacaoIa(avaliacao);
            }
        }
        ideias.saveAll(lote);
    }

    private static AvaliacaoIaResponse paraResponse(Ideia ideia) {
        return new AvaliacaoIaResponse(ideia.getId(), ideia.getTitulo(), ideia.getAutor().nome(), ideia.getStatus(),
                ideia.getPrioridade(), ideia.getAvaliacaoIa());
    }
}
