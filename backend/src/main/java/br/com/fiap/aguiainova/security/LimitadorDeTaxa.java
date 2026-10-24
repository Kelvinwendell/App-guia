package br.com.fiap.aguiainova.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

/**
 * Limite de requisições por janela de tempo fixa, em memória (sem Redis/serviço externo: esta API roda numa
 * instância só). Cada chave (ex.: e-mail de login, id de usuário) tem sua própria janela; passado o tempo da
 * janela, a contagem reinicia. Não é exato sob concorrência extrema (ligeira folga possível na borda da janela),
 * o que é aceitável para o que isto protege: força bruta de login e abuso de custo de IA, não um limite de cobrança.
 */
@Component
public class LimitadorDeTaxa {

    private final Clock clock;
    private final ConcurrentHashMap<String, Janela> janelas = new ConcurrentHashMap<>();

    public LimitadorDeTaxa(Clock clock) {
        this.clock = clock;
    }

    /** @return true se a requisição pode seguir; false se a chave já excedeu o limite nesta janela. */
    public boolean permitir(String chave, int limite, Duration duracaoJanela) {
        Instant agora = clock.instant();
        Janela janela = janelas.compute(chave, (k, atual) -> {
            if (atual == null || !agora.isBefore(atual.expiraEm)) {
                return new Janela(agora.plus(duracaoJanela), new AtomicInteger(0));
            }
            return atual;
        });
        return janela.contagem.incrementAndGet() <= limite;
    }

    private record Janela(Instant expiraEm, AtomicInteger contagem) {
    }
}
