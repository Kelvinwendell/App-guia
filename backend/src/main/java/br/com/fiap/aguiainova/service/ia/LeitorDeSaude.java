package br.com.fiap.aguiainova.service.ia;

import br.com.fiap.aguiainova.model.DiagnosticoEstrategia;

/** Estrategia de leitura da saude de uma estrategia: IA generativa, regras locais ou uma combinacao. */
public interface LeitorDeSaude {

    /** Explica o diagnostico (nivel e motivos ja calculados por regras) e recomenda o que fazer. */
    LeituraGerada ler(DiagnosticoEstrategia diagnostico);
}
