package br.com.fiap.aguiainova.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.com.fiap.aguiainova.model.enums.StatusEstrategia;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "estrategias")
public class Estrategia extends DocumentoBase {

    private String titulo;
    private String descricao;
    private String categoria;
    private String campanha;
    private StatusEstrategia status;
    private List<String> metas = new ArrayList<>();
    private LocalDate inicio;
    private LocalDate fim;
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
        this.metas = new ArrayList<>(dados.metas());
        this.inicio = dados.inicio();
        this.fim = dados.fim();
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

    /** Visao somente leitura; documentos antigos, gravados antes das metas existirem, devolvem lista vazia. */
    public List<String> getMetas() {
        return metas == null ? List.of() : Collections.unmodifiableList(metas);
    }

    public LocalDate getInicio() {
        return inicio;
    }

    public LocalDate getFim() {
        return fim;
    }

    public Autor getAutor() {
        return autor;
    }
}
