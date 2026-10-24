package br.com.fiap.aguiainova.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** O que a liderança quer alcançar, em linguagem livre; a IA devolve um rascunho de estratégia. */
public record RascunhoEstrategiaRequest(@NotBlank @Size(max = 1000) String objetivo) {
}
