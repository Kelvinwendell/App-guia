package br.com.fiap.aguiainova.model;

/**
 * Quem praticou uma acao (criou uma ideia, avaliou um projeto...). Guarda o nome junto do id para que o historico
 * continue legivel mesmo se o usuario for alterado depois.
 */
public record Autor(String usuarioId, String nome) {
}
