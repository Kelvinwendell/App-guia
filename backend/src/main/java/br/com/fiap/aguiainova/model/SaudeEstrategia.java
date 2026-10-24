package br.com.fiap.aguiainova.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import br.com.fiap.aguiainova.model.enums.NivelSaude;
import br.com.fiap.aguiainova.util.Numeros;

/**
 * Saude de uma estrategia vigente, apurada por regras (nunca pela IA, que so explica o resultado). Fonte unica das
 * regras, como {@link ProjetoMetricas} e das formulas.
 *
 * <ul>
 *   <li>EM_RISCO: periodo vencido com a estrategia ainda vigente, projeto atrasado, duas ou mais ideias aprovadas
 *       sem projeto, ou ROI negativo com projeto ja concluido;</li>
 *   <li>SEM_TRACAO (se nao ha risco): nenhum projeto em aberto e nenhuma ideia ou nenhuma atividade ha mais de
 *       60 dias; estrategias com ate 14 dias de vida ou que ainda nao comecaram ganham carencia;</li>
 *   <li>EM_DIA: o restante.</li>
 * </ul>
 *
 * @param motivos     o que levou ao nivel (e observacoes que merecem atencao mesmo estando em dia)
 * @param proximaAcao a acao mais util agora, em uma frase
 */
public record SaudeEstrategia(NivelSaude nivel, List<String> motivos, String proximaAcao) {

    static final int CARENCIA_DIAS = 14;
    static final int JANELA_SEM_ATIVIDADE_DIAS = 60;
    static final int APROVADAS_SEM_PROJETO_CRITICO = 2;

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static SaudeEstrategia avaliar(IndicadoresEstrategia indicadores, LocalDate hoje) {
        Situacao situacao = Situacao.de(indicadores, hoje);
        NivelSaude nivel = situacao.emRisco() ? NivelSaude.EM_RISCO
                : situacao.parada() ? NivelSaude.SEM_TRACAO : NivelSaude.EM_DIA;
        return new SaudeEstrategia(nivel, motivos(indicadores, situacao, nivel), proximaAcao(indicadores, situacao));
    }

    private static List<String> motivos(IndicadoresEstrategia i, Situacao s, NivelSaude nivel) {
        List<String> motivos = new ArrayList<>();
        if (nivel == NivelSaude.EM_DIA && (i.ideias() > 0 || i.projetos() > 0)) {
            motivos.add("%d ideia(s) e %d projeto(s), %d em aberto, sem atrasos."
                    .formatted(i.ideias(), i.projetos(), i.projetosEmAberto()));
        }
        if (s.naoComecou()) {
            motivos.add("O período começa em %s.".formatted(i.inicio().format(DATA)));
        }
        if (s.periodoVencido()) {
            motivos.add("O período terminou em %s e a estratégia continua vigente.".formatted(i.fim().format(DATA)));
        }
        if (i.projetosAtrasados() > 0) {
            motivos.add("%d projeto(s) com o prazo vencido.".formatted(i.projetosAtrasados()));
        }
        if (i.aprovadasSemProjeto() > 0) {
            motivos.add("%d ideia(s) aprovada(s) ainda sem projeto.".formatted(i.aprovadasSemProjeto()));
        }
        if (s.roiNegativo()) {
            motivos.add("ROI de %s mesmo com projeto(s) já concluído(s).".formatted(Numeros.percentual(i.roiPercentual())));
        }
        if (s.parada()) {
            motivos.add(s.semVinculos()
                    ? "Nenhuma ideia ou projeto vinculado desde que foi criada."
                    : "Sem atividade há %d dias e nenhum projeto em aberto.".formatted(s.diasSemAtividade()));
        } else if (s.semVinculos() && !s.naoComecou()) {
            motivos.add("Estratégia recente: ainda sem ideias ou projetos vinculados.");
        }
        if (i.metas() == 0) {
            motivos.add("Sem metas definidas: o avanço não pode ser medido.");
        }
        return List.copyOf(motivos);
    }

    private static String proximaAcao(IndicadoresEstrategia i, Situacao s) {
        if (i.projetosAtrasados() > 0) {
            return "Reveja o prazo ou reforce a equipe dos projetos atrasados.";
        }
        if (s.periodoVencido()) {
            return "Encerre a estratégia ou prorrogue o período.";
        }
        if (i.aprovadasSemProjeto() > 0) {
            return "Transforme as ideias aprovadas em projetos.";
        }
        if (s.parada()) {
            return s.semVinculos()
                    ? "Divulgue a estratégia para gerar ideias alinhadas ou avalie encerrá-la."
                    : "Estimule novas ideias ou projetos alinhados a esta estratégia, ou avalie encerrá-la.";
        }
        if (s.roiNegativo()) {
            return "Reveja escopo, prazo e investimento dos projetos.";
        }
        if (i.metas() == 0) {
            return "Defina metas mensuráveis para acompanhar o avanço.";
        }
        if (s.semVinculos()) {
            return "Divulgue a estratégia para que os colaboradores enviem ideias alinhadas.";
        }
        return "Mantenha o acompanhamento e registre os resultados dos projetos.";
    }

    /** Leitura dos indicadores em perguntas de sim/nao, para que as regras de nivel e de texto usem a mesma. */
    private record Situacao(boolean naoComecou, boolean periodoVencido, boolean semVinculos, boolean parada,
            boolean roiNegativo, boolean emRisco, long diasSemAtividade) {

        static Situacao de(IndicadoresEstrategia i, LocalDate hoje) {
            boolean naoComecou = i.inicio() != null && i.inicio().isAfter(hoje);
            boolean periodoVencido = i.fim() != null && i.fim().isBefore(hoje);
            boolean semVinculos = i.ideias() == 0 && i.projetos() == 0;
            boolean recente = ChronoUnit.DAYS.between(i.criadaEm(), hoje) <= CARENCIA_DIAS;
            long diasSemAtividade = i.ultimaAtividade() == null ? 0 : ChronoUnit.DAYS.between(i.ultimaAtividade(), hoje);
            boolean semMovimento = semVinculos ? !recente : diasSemAtividade > JANELA_SEM_ATIVIDADE_DIAS;
            boolean parada = !naoComecou && i.projetosEmAberto() == 0 && semMovimento;
            boolean roiNegativo = i.roiPercentual() != null && i.roiPercentual().signum() < 0
                    && i.projetosConcluidos() > 0;
            boolean emRisco = periodoVencido || i.projetosAtrasados() > 0 || roiNegativo
                    || i.aprovadasSemProjeto() >= APROVADAS_SEM_PROJETO_CRITICO;
            return new Situacao(naoComecou, periodoVencido, semVinculos, parada, roiNegativo, emRisco,
                    diasSemAtividade);
        }
    }
}
