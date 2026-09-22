package br.com.fiap.aguiainova.model.enums;

public enum StatusProjeto {
    PLANEJAMENTO,
    EM_ANDAMENTO,
    CONCLUIDO,
    PAUSADO,
    CANCELADO;

    /** Projeto que ainda pode atrasar ou gerar resultado (nem concluido, nem cancelado). */
    public boolean emAberto() {
        return this != CONCLUIDO && this != CANCELADO;
    }
}
