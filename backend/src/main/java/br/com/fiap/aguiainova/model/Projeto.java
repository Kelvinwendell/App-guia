package br.com.fiap.aguiainova.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import br.com.fiap.aguiainova.exception.NaoEncontradoException;
import br.com.fiap.aguiainova.exception.RegraDeNegocioException;
import br.com.fiap.aguiainova.exception.RequisicaoInvalidaException;
import br.com.fiap.aguiainova.model.enums.EtapaProjeto;
import br.com.fiap.aguiainova.model.enums.StatusProjeto;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Projeto/iniciativa acompanhado pelos gestores. Guarda seus proprios invariantes: progresso entre 0 e 100,
 * projeto concluido sempre com 100% e resultados so alteraveis pelos metodos da classe.
 */
@Document(collection = "projetos")
public class Projeto extends DocumentoBase {

    private static final int PROGRESSO_MINIMO = 0;
    private static final int PROGRESSO_CONCLUIDO = 100;

    private String nome;
    private String descricao;
    @Indexed
    private StatusProjeto status;
    @Indexed
    private EtapaProjeto etapa;
    private BigDecimal investimento;
    private LocalDate dataInicio;
    private LocalDate prazo;
    private int progresso;
    @Indexed
    private String estrategiaId;
    private String ideiaId;
    private Autor autor;
    private List<ResultadoProjeto> resultados = new ArrayList<>();

    /** Exigido pelo Spring Data para reidratar o documento; o codigo da aplicacao usa o construtor publico. */
    protected Projeto() {
    }

    public Projeto(DadosProjeto dados, Autor autor) {
        this.autor = autor;
        this.status = StatusProjeto.PLANEJAMENTO;
        this.etapa = EtapaProjeto.INICIACAO;
        atualizarDados(dados);
    }

    /** Reaplica os dados cadastrais; status e etapa so mudam se informados. */
    public void atualizarDados(DadosProjeto dados) {
        this.nome = dados.nome();
        this.descricao = dados.descricao();
        this.investimento = dados.investimento();
        this.dataInicio = dados.dataInicio();
        this.prazo = dados.prazo();
        this.estrategiaId = dados.estrategiaId();
        this.ideiaId = dados.ideiaId();
        if (dados.status() != null) {
            this.status = dados.status();
        }
        if (dados.etapa() != null) {
            this.etapa = dados.etapa();
        }
        alinharProgressoAoStatus();
    }

    /** Atualiza o andamento; os valores nulos permanecem como estao, mas ao menos um deve ser informado. */
    public void atualizarAndamento(StatusProjeto novoStatus, EtapaProjeto novaEtapa, Integer novoProgresso) {
        if (novoStatus == null && novaEtapa == null && novoProgresso == null) {
            throw new RequisicaoInvalidaException("Informe status, etapa e/ou progresso.");
        }
        if (novoStatus != null) {
            this.status = novoStatus;
        }
        if (novaEtapa != null) {
            this.etapa = novaEtapa;
        }
        if (novoProgresso != null) {
            exigirProgressoValido(novoProgresso);
            this.progresso = novoProgresso;
        }
        alinharProgressoAoStatus();
    }

    public void registrarResultado(DadosResultado dados, Autor registradoPor) {
        resultados.add(new ResultadoProjeto(UUID.randomUUID().toString(), dados.data(), dados.descricao(),
                dados.retornoFinanceiro(), dados.ganhoProdutividade(), dados.reducaoCustos(), registradoPor));
    }

    public void removerResultado(String resultadoId) {
        boolean removido = resultados.removeIf(resultado -> resultado.id().equals(resultadoId));
        if (!removido) {
            throw new NaoEncontradoException("Resultado não encontrado neste projeto.");
        }
    }

    /** Atrasado = ainda em aberto (nem concluido, nem cancelado) e com o prazo ja vencido. */
    public boolean estaAtrasado(LocalDate hoje) {
        return status.emAberto() && prazo.isBefore(hoje);
    }

    /** Dias ate o prazo; negativo quando o prazo ja passou. */
    public long diasParaPrazo(LocalDate hoje) {
        return ChronoUnit.DAYS.between(hoje, prazo);
    }

    private void alinharProgressoAoStatus() {
        if (status == StatusProjeto.CONCLUIDO) {
            this.progresso = PROGRESSO_CONCLUIDO;
        }
    }

    private static void exigirProgressoValido(int valor) {
        if (valor < PROGRESSO_MINIMO || valor > PROGRESSO_CONCLUIDO) {
            throw new RegraDeNegocioException("O progresso deve estar entre 0 e 100.");
        }
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public StatusProjeto getStatus() {
        return status;
    }

    public EtapaProjeto getEtapa() {
        return etapa;
    }

    public BigDecimal getInvestimento() {
        return investimento;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public LocalDate getPrazo() {
        return prazo;
    }

    public int getProgresso() {
        return progresso;
    }

    public String getEstrategiaId() {
        return estrategiaId;
    }

    public String getIdeiaId() {
        return ideiaId;
    }

    public Autor getAutor() {
        return autor;
    }

    /** Visao somente leitura: quem quiser alterar os resultados usa registrarResultado/removerResultado. */
    public List<ResultadoProjeto> getResultados() {
        return Collections.unmodifiableList(resultados);
    }
}
