package br.com.fiap.aguiainova.model;

import java.util.Locale;

import br.com.fiap.aguiainova.model.enums.Perfil;
import br.com.fiap.aguiainova.util.Textos;
import br.com.fiap.aguiainova.util.Validacoes;

/** Dados de cadastro de um usuario, ja normalizados (e-mail em minusculas, textos sem espacos nas pontas). */
public record DadosUsuario(String nome, String email, String senhaHash, Perfil perfil, String cargo,
        String departamento, String unidade) {

    public DadosUsuario {
        nome = Validacoes.exigirTexto(nome, "nome");
        email = Validacoes.exigirTexto(email, "e-mail").toLowerCase(Locale.ROOT);
        senhaHash = Validacoes.exigirTexto(senhaHash, "senha");
        perfil = Validacoes.exigir(perfil, "perfil");
        cargo = Textos.ouNulo(cargo);
        departamento = Textos.ouNulo(departamento);
        unidade = Textos.ouNulo(unidade);
    }
}
