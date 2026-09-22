package br.com.fiap.aguiainova.model;

import java.time.Instant;

import br.com.fiap.aguiainova.model.enums.AcaoEstrategia;
import br.com.fiap.aguiainova.model.enums.StatusEstrategia;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Registro imutavel de cada mudanca em uma estrategia (id, data, categoria e campanha, alem de um retrato do
 * conteudo). Continua existindo mesmo depois que a estrategia e removida.
 */
@Document(collection = "estrategias_historico")
public class EstrategiaHistorico {

    @Id
    private String id;
    @Indexed
    private String estrategiaId;
    @Indexed
    private Instant data;
    private String categoria;
    private String campanha;
    private AcaoEstrategia acao;
    private String titulo;
    private String descricao;
    private StatusEstrategia status;
    private Autor autor;

    /** Exigido pelo Spring Data para reidratar o documento; o codigo da aplicacao usa {@link #registrar}. */
    protected EstrategiaHistorico() {
    }

    private EstrategiaHistorico(Estrategia estrategia, AcaoEstrategia acao, Autor autor, Instant data) {
        this.estrategiaId = estrategia.getId();
        this.data = data;
        this.categoria = estrategia.getCategoria();
        this.campanha = estrategia.getCampanha();
        this.acao = acao;
        this.titulo = estrategia.getTitulo();
        this.descricao = estrategia.getDescricao();
        this.status = estrategia.getStatus();
        this.autor = autor;
    }

    /** Tira um retrato da estrategia no momento da acao. */
    public static EstrategiaHistorico registrar(Estrategia estrategia, AcaoEstrategia acao, Autor autor,
            Instant data) {
        return new EstrategiaHistorico(estrategia, acao, autor, data);
    }

    public String getId() {
        return id;
    }

    public String getEstrategiaId() {
        return estrategiaId;
    }

    public Instant getData() {
        return data;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getCampanha() {
        return campanha;
    }

    public AcaoEstrategia getAcao() {
        return acao;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public StatusEstrategia getStatus() {
        return status;
    }

    public Autor getAutor() {
        return autor;
    }
}
