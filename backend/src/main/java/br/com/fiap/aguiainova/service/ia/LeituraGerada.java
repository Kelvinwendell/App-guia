package br.com.fiap.aguiainova.service.ia;

import java.util.List;

/** @param fonte GEMINI, OPENAI ou HEURISTICA */
public record LeituraGerada(String leitura, List<String> recomendacoes, String fonte) {
}
