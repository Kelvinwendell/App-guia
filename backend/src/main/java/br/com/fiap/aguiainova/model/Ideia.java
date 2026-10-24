package br.com.fiap.aguiainova.model;

import java.time.Instant;

import br.com.fiap.aguiainova.exception.ConflitoException;
import br.com.fiap.aguiainova.exception.RequisicaoInvalidaException;
import br.com.fiap.aguiainova.model.enums.Prioridade;
import br.com.fiap.aguiainova.model.enums.StatusIdeia;
import br.com.fiap.aguiainova.util.Textos;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Ideia de inovacao de um colaborador. Nasce EM_ANALISE e so o gestor decide seu destino; enquanto estiver em
 * analise o autor pode revisa-la ou exclui-la, depois disso ela fica protegida.
 */
@Document(collection = "ideias")
@CompoundIndex(name = "idx_ideia_autor", def = "{'autor.usuarioId': 1}")
public class Ideia extends DocumentoBase {

    private String titulo;
    private String descricao;
    private String categoria;
    @Indexed
    private StatusIdeia status;
    private Prioridade prioridade;
    private Autor autor;
    @Indexed
    private String estrategiaId;
    private String parecer;
    private Autor avaliador;
    private Instant avaliadaEm;
    private AvaliacaoIa avaliacaoIa;

    /** Exigido pelo Spring Data para reidratar o documento; o codigo da aplicacao usa o construtor publico. */
    protected Ideia() {
    }

    public Ideia(DadosIdeia dados, Autor autor) {
        this.autor = autor;
        this.status = StatusIdeia.EM_ANALISE;
        this.prioridade = Prioridade.MEDIA;
        aplicar(dados);
    }

    /** Revisao feita pelo autor. A nota da IA e descartada porque avaliou o texto anterior. */
    public void revisar(DadosIdeia dados) {
        exigirEmAnalise("alterada");
        aplicar(dados);
        this.avaliacaoIa = null;
    }

    public void validarRemocao() {
        exigirEmAnalise("excluída");
    }

    /** Decisao do gestor: aprova/rejeita, define a prioridade e/ou deixa um parecer. */
    public void avaliar(StatusIdeia novoStatus, Prioridade novaPrioridade, String novoParecer, Autor gestor,
            Instant quando) {
        String parecerNormalizado = Textos.ouNulo(novoParecer);
        if (novoStatus == null && novaPrioridade == null && parecerNormalizado == null) {
            throw new RequisicaoInvalidaException("Informe status, prioridade e/ou parecer.");
        }
        if (novoStatus != null) {
            this.status = novoStatus;
        }
        if (novaPrioridade != null) {
            this.prioridade = novaPrioridade;
        }
        if (parecerNormalizado != null) {
            this.parecer = parecerNormalizado;
        }
        this.avaliador = gestor;
        this.avaliadaEm = quando;
    }

    public void registrarAvaliacaoIa(AvaliacaoIa avaliacao) {
        this.avaliacaoIa = avaliacao;
    }

    public boolean estaEmAnalise() {
        return status == StatusIdeia.EM_ANALISE;
    }

    public boolean pertenceA(String usuarioId) {
        return autor != null && autor.usuarioId().equals(usuarioId);
    }

    private void aplicar(DadosIdeia dados) {
        this.titulo = dados.titulo();
        this.descricao = dados.descricao();
        this.categoria = dados.categoria();
        this.estrategiaId = dados.estrategiaId();
    }

    private void exigirEmAnalise(String acao) {
        if (!estaEmAnalise()) {
            throw new ConflitoException("Uma ideia já avaliada pelo gestor não pode ser " + acao + ".");
        }
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

    public StatusIdeia getStatus() {
        return status;
    }

    public Prioridade getPrioridade() {
        return prioridade;
    }

    public Autor getAutor() {
        return autor;
    }

    public String getEstrategiaId() {
        return estrategiaId;
    }

    public String getParecer() {
        return parecer;
    }

    public Autor getAvaliador() {
        return avaliador;
    }

    public Instant getAvaliadaEm() {
        return avaliadaEm;
    }

    public AvaliacaoIa getAvaliacaoIa() {
        return avaliacaoIa;
    }
}
