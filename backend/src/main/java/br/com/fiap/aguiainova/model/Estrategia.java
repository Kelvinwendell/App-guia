package br.com.fiap.aguiainova.model;

import br.com.fiap.aguiainova.model.enums.StatusEstrategia;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "estrategias")
public class Estrategia extends DocumentoBase {

    private String titulo;
    private String descricao;
    private String categoria;
    private String campanha;
    @Indexed
    private StatusEstrategia status;
    private Autor autor;

    /** Exigido pelo Spring Data para reidratar o documento; o codigo da aplicacao usa o construtor publico. */
    protected Estrategia() {
    }

    public Estrategia(DadosEstrategia dados, Autor autor) {
        this.autor = autor;
        this.status = StatusEstrategia.VIGENTE;
        atualizar(dados);
    }

    /** Aplica o novo conteudo; o status so muda se informado. */
    public void atualizar(DadosEstrategia dados) {
        this.titulo = dados.titulo();
        this.descricao = dados.descricao();
        this.categoria = dados.categoria();
        this.campanha = dados.campanha();
        if (dados.status() != null) {
            this.status = dados.status();
        }
    }

    public boolean estaVigente() {
        return status == StatusEstrategia.VIGENTE;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getCampanha() {
        return campanha;
    }

    public StatusEstrategia getStatus() {
        return status;
    }

    public Autor getAutor() {
        return autor;
    }
}
