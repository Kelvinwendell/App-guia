package br.com.fiap.aguiainova.service.ia;

import java.util.List;

import br.com.fiap.aguiainova.model.DiagnosticoEstrategia;
import br.com.fiap.aguiainova.model.SaudeEstrategia;
import org.springframework.stereotype.Component;

/** Leitura da saude por regras: plano de contingencia que reaproveita os motivos e a proxima acao ja calculados. */
@Component
public class LeitorSaudeHeuristico implements LeitorDeSaude {

    @Override
    public LeituraGerada ler(DiagnosticoEstrategia diagnostico) {
        SaudeEstrategia saude = diagnostico.saude();
        String titulo = diagnostico.estrategia().getTitulo();
        String abertura = switch (saude.nivel()) {
            case EM_DIA -> "A estratégia \"%s\" está em dia.".formatted(titulo);
            case EM_RISCO -> "A estratégia \"%s\" exige atenção.".formatted(titulo);
            case SEM_TRACAO -> "A estratégia \"%s\" ainda não ganhou tração.".formatted(titulo);
        };
        String leitura = (abertura + " " + String.join(" ", saude.motivos())).strip();
        return new LeituraGerada(leitura, List.of(saude.proximaAcao()), FonteIa.HEURISTICA);
    }
}
