package br.com.fiap.aguiainova.model;

import br.com.fiap.aguiainova.model.enums.Perfil;
import br.com.fiap.aguiainova.util.Validacoes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "usuarios")
public class Usuario extends DocumentoBase {

    private String nome;
    @Indexed(unique = true)
    private String email;
    private String senhaHash;
    private Perfil perfil;
    private String cargo;
    private String departamento;
    private String unidade;
    private boolean ativo = true;

    /** Exigido pelo Spring Data para reidratar o documento; o codigo da aplicacao usa o construtor publico. */
    protected Usuario() {
    }

    public Usuario(DadosUsuario dados) {
        this.nome = dados.nome();
        this.email = dados.email();
        this.senhaHash = dados.senhaHash();
        this.perfil = dados.perfil();
        this.cargo = dados.cargo();
        this.departamento = dados.departamento();
        this.unidade = dados.unidade();
    }

    public void alterarPerfil(Perfil novoPerfil) {
        this.perfil = Validacoes.exigir(novoPerfil, "perfil");
    }

    public Autor comoAutor() {
        return new Autor(getId(), nome);
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public String getCargo() {
        return cargo;
    }

    public String getDepartamento() {
        return departamento;
    }

    public String getUnidade() {
        return unidade;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
