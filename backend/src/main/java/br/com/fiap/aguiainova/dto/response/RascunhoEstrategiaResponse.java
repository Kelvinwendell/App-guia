package br.com.fiap.aguiainova.dto.response;

import java.time.LocalDate;
import java.util.List;

/**
 * Rascunho de estratégia escrito pela IA; a liderança revisa e salva. Respeita os limites de {@code EstrategiaRequest}.
 *
 * @param campanha nulo quando o modelo não sugeriu uma
 * @param inicio   nulo quando o modelo não sugeriu um período válido
 * @param fonte    GEMINI ou OPENAI
 */
public record RascunhoEstrategiaResponse(
        String titulo,
        String descricao,
        String categoria,
        String campanha,
        List<String> metas,
        LocalDate inicio,
        LocalDate fim,
        String fonte) {
}
