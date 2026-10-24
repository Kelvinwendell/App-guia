package br.com.fiap.aguiainova.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Estratégia a ser criticada antes de salvar.
 *
 * @param estrategiaId opcional: id da estratégia que está sendo editada, para que ela não seja comparada consigo mesma
 */
public record AnaliseEstrategiaRequest(String estrategiaId, @Valid @NotNull EstrategiaRequest estrategia) {
}
