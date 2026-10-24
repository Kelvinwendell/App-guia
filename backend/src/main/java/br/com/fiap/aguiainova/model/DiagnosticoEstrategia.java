package br.com.fiap.aguiainova.model;

/** Uma estrategia junto dos numeros que a descrevem e da saude apurada a partir deles. */
public record DiagnosticoEstrategia(Estrategia estrategia, IndicadoresEstrategia indicadores, SaudeEstrategia saude) {
}
