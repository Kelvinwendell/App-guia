package br.com.fiap.aguiainova.model.enums;

/** Situacao de uma estrategia vigente, apurada por regras a partir das ideias e projetos vinculados a ela. */
public enum NivelSaude {
    /** Ha movimento e nada fora do esperado. */
    EM_DIA,
    /** Ha algo que pede atencao: prazos vencidos, ideias aprovadas paradas, periodo expirado, retorno negativo. */
    EM_RISCO,
    /** Ninguem esta trabalhando nela: sem ideias, sem projetos em aberto ou sem atividade recente. */
    SEM_TRACAO
}
