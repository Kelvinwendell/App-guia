package br.com.fiap.aguiainova.service.ia;

import java.util.List;

import br.com.fiap.aguiainova.model.DadosEstrategia;
import br.com.fiap.aguiainova.model.Estrategia;

/** Estrategia de critica de uma estrategia ainda nao publicada: IA generativa, regras locais ou uma combinacao. */
public interface AnalisadorDeEstrategias {

    /**
     * @param outrasVigentes as demais estrategias vigentes (a que esta sendo analisada, se ja existe, fica de fora),
     *                       usadas para detectar sobreposicao
     */
    AnaliseGerada analisar(DadosEstrategia dados, List<Estrategia> outrasVigentes);
}
