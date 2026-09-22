package br.com.fiap.aguiainova.model.enums;

public enum Prioridade {
    BAIXA,
    MEDIA,
    ALTA;

    private static final int SCORE_MINIMO_ALTA = 70;
    private static final int SCORE_MINIMO_MEDIA = 45;

    /** Prioridade sugerida para uma pontuacao de 0 a 100 (regra unica, usada pela IA e pela heuristica). */
    public static Prioridade sugeridaParaScore(int score) {
        if (score >= SCORE_MINIMO_ALTA) {
            return ALTA;
        }
        return score >= SCORE_MINIMO_MEDIA ? MEDIA : BAIXA;
    }
}
