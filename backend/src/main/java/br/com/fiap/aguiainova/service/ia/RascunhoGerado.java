package br.com.fiap.aguiainova.service.ia;

import java.time.LocalDate;
import java.util.List;

/**
 * Rascunho de estrategia escrito pela IA a partir de um objetivo. E so uma sugestao: a lideranca revisa antes de salvar.
 *
 * @param campanha nulo quando o modelo nao sugeriu uma
 * @param inicio   nulo quando o modelo nao sugeriu um periodo valido
 * @param fonte    GEMINI ou OPENAI (nao ha rascunho por regras)
 */
public record RascunhoGerado(String titulo, String descricao, String categoria, String campanha, List<String> metas,
        LocalDate inicio, LocalDate fim, String fonte) {
}
