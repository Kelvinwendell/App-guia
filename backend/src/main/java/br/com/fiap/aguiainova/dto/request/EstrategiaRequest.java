package br.com.fiap.aguiainova.dto.request;

import java.time.LocalDate;
import java.util.List;

import br.com.fiap.aguiainova.model.DadosEstrategia;
import br.com.fiap.aguiainova.model.enums.StatusEstrategia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EstrategiaRequest(
        @NotBlank @Size(max = 150) String titulo,
        @NotBlank @Size(max = 2000) String descricao,
        @NotBlank @Size(max = 80) String categoria,
        @Size(max = 120) String campanha,
        /** Opcional; se omitido, uma estratégia nova nasce VIGENTE e uma existente mantém o status. */
        StatusEstrategia status,
        /** Opcional: resultados esperados, de preferência mensuráveis (itens em branco são descartados). */
        @Size(max = DadosEstrategia.MAXIMO_METAS) List<@Size(max = 200) String> metas,
        /** Opcional: início do período de vigência (yyyy-MM-dd). */
        LocalDate inicio,
        /** Opcional: fim do período de vigência (yyyy-MM-dd), não anterior ao início. */
        LocalDate fim) {

    /** Estratégia sem metas nem período. */
    public EstrategiaRequest(String titulo, String descricao, String categoria, String campanha,
            StatusEstrategia status) {
        this(titulo, descricao, categoria, campanha, status, null, null, null);
    }
}
