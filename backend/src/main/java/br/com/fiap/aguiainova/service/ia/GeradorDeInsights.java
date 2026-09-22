package br.com.fiap.aguiainova.service.ia;

/** Estrategia de leitura dos resultados do dashboard: IA generativa, regras locais ou uma combinacao. */
public interface GeradorDeInsights {

    InsightsGerados gerar(IndicadoresDashboard indicadores);
}
