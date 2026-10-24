package br.com.fiap.aguiainova.support;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.com.fiap.aguiainova.model.IndicadoresEstrategia;

/**
 * Monta {@link IndicadoresEstrategia} para os testes das regras de saude. O ponto de partida e uma estrategia
 * saudavel (movimento recente, um projeto em aberto, sem atrasos); cada teste muda so o que quer provar.
 */
public final class IndicadoresBuilder {

    public static final LocalDate HOJE = LocalDate.of(2026, 10, 24);

    private LocalDate criadaEm = HOJE.minusDays(100);
    private LocalDate inicio;
    private LocalDate fim;
    private int metas = 2;
    private long ideias = 3;
    private long ideiasEmAnalise;
    private long aprovadasSemProjeto;
    private int projetos = 2;
    private int projetosEmAberto = 1;
    private int projetosAtrasados;
    private int projetosConcluidos = 1;
    private BigDecimal investimento = new BigDecimal("1000.00");
    private BigDecimal retorno = new BigDecimal("1500.00");
    private BigDecimal roiPercentual = new BigDecimal("50.00");
    private LocalDate ultimaAtividade = HOJE.minusDays(3);

    public static IndicadoresBuilder saudavel() {
        return new IndicadoresBuilder();
    }

    /** Estrategia sem nenhuma ideia ou projeto vinculado. */
    public static IndicadoresBuilder semVinculos() {
        return new IndicadoresBuilder().ideias(0).projetos(0, 0, 0, 0).ultimaAtividade(null)
                .financeiro(BigDecimal.ZERO, BigDecimal.ZERO, null);
    }

    public IndicadoresBuilder criadaEm(LocalDate valor) {
        this.criadaEm = valor;
        return this;
    }

    public IndicadoresBuilder periodo(LocalDate inicio, LocalDate fim) {
        this.inicio = inicio;
        this.fim = fim;
        return this;
    }

    public IndicadoresBuilder metas(int valor) {
        this.metas = valor;
        return this;
    }

    public IndicadoresBuilder ideias(long total) {
        this.ideias = total;
        return this;
    }

    public IndicadoresBuilder aprovadasSemProjeto(long valor) {
        this.aprovadasSemProjeto = valor;
        return this;
    }

    public IndicadoresBuilder projetos(int total, int emAberto, int atrasados, int concluidos) {
        this.projetos = total;
        this.projetosEmAberto = emAberto;
        this.projetosAtrasados = atrasados;
        this.projetosConcluidos = concluidos;
        return this;
    }

    public IndicadoresBuilder financeiro(BigDecimal investimento, BigDecimal retorno, BigDecimal roiPercentual) {
        this.investimento = investimento;
        this.retorno = retorno;
        this.roiPercentual = roiPercentual;
        return this;
    }

    public IndicadoresBuilder ultimaAtividade(LocalDate valor) {
        this.ultimaAtividade = valor;
        return this;
    }

    public IndicadoresEstrategia build() {
        return new IndicadoresEstrategia(criadaEm, inicio, fim, metas, ideias, ideiasEmAnalise, aprovadasSemProjeto,
                projetos, projetosEmAberto, projetosAtrasados, projetosConcluidos, investimento, retorno,
                roiPercentual, ultimaAtividade);
    }
}
