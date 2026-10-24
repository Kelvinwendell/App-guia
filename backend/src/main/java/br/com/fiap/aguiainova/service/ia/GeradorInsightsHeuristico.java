package br.com.fiap.aguiainova.service.ia;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import br.com.fiap.aguiainova.dto.response.DesempenhoEstrategiaResponse;
import br.com.fiap.aguiainova.dto.response.DesempenhoProjetoResponse;
import br.com.fiap.aguiainova.dto.response.ResumoDashboardResponse;
import br.com.fiap.aguiainova.dto.response.ResumoIdeiasResponse;
import br.com.fiap.aguiainova.model.enums.StatusProjeto;
import br.com.fiap.aguiainova.util.Numeros;
import org.springframework.stereotype.Component;

/** Leitura do dashboard por regras simples: plano de contingencia quando a IA generativa nao esta disponivel. */
@Component
public class GeradorInsightsHeuristico implements GeradorDeInsights {

    private static final Locale PT_BR = Locale.of("pt", "BR");
    private static final String MENSAGEM_SEM_DADOS =
            "Ainda não há dados suficientes para gerar análises: cadastre projetos e registre seus resultados.";

    @Override
    public InsightsGerados gerar(IndicadoresDashboard indicadores) {
        ResumoDashboardResponse resumo = indicadores.resumo();
        List<String> insights = new ArrayList<>();
        List<String> recomendacoes = new ArrayList<>();
        analisarRoi(resumo, insights, recomendacoes);
        analisarPrazos(resumo, insights, recomendacoes);
        analisarEstrategias(indicadores.porEstrategia(), insights, recomendacoes);
        analisarIdeias(resumo.ideias(), insights, recomendacoes);
        analisarProjetosSemResultado(indicadores.porProjeto(), insights, recomendacoes);
        if (insights.isEmpty()) {
            insights.add(MENSAGEM_SEM_DADOS);
        }
        return new InsightsGerados(panorama(resumo), insights, recomendacoes, FonteIa.HEURISTICA);
    }

    private static String panorama(ResumoDashboardResponse resumo) {
        return "Panorama: %d projeto(s), investimento de %s, retorno de %s e ROI de %s."
                .formatted(resumo.totalProjetos(), moeda(resumo.investimentoTotal()), moeda(resumo.retornoTotal()),
                        percentual(resumo.roiPercentual()));
    }

    private static void analisarRoi(ResumoDashboardResponse resumo, List<String> insights,
            List<String> recomendacoes) {
        BigDecimal roi = resumo.roiPercentual();
        if (roi == null) {
            return;
        }
        if (roi.signum() < 0) {
            insights.add("O ROI geral está em %s: o retorno acumulado (%s) ainda não cobre o investimento (%s)."
                    .formatted(percentual(roi), moeda(resumo.retornoTotal()), moeda(resumo.investimentoTotal())));
            recomendacoes.add("Priorize os projetos com melhor ROI e reavalie os que consomem investimento sem gerar resultado.");
        } else {
            insights.add("O ROI geral é positivo (%s), com lucro acumulado de %s."
                    .formatted(percentual(roi), moeda(resumo.lucroTotal())));
            recomendacoes.add("Documente os projetos de maior retorno e replique o modelo em novas iniciativas.");
        }
    }

    private static void analisarPrazos(ResumoDashboardResponse resumo, List<String> insights,
            List<String> recomendacoes) {
        if (resumo.projetosAtrasados() > 0) {
            insights.add("%d projeto(s) estão com o prazo vencido.".formatted(resumo.projetosAtrasados()));
            recomendacoes.add("Renegocie prazos ou reforce a equipe dos projetos atrasados antes que impactem o retorno.");
        }
    }

    private static void analisarEstrategias(List<DesempenhoEstrategiaResponse> estrategias, List<String> insights,
            List<String> recomendacoes) {
        List<DesempenhoEstrategiaResponse> comRoi = estrategias.stream()
                .filter(e -> e.projetos() > 0 && e.roiPercentual() != null)
                .toList();
        Optional<DesempenhoEstrategiaResponse> melhor = comRoi.stream()
                .max(Comparator.comparing(DesempenhoEstrategiaResponse::roiPercentual));
        Optional<DesempenhoEstrategiaResponse> pior = comRoi.stream()
                .min(Comparator.comparing(DesempenhoEstrategiaResponse::roiPercentual));
        melhor.ifPresent(e -> insights.add("A estratégia \"%s\" tem o melhor ROI (%s).".formatted(e.titulo(),
                percentual(e.roiPercentual()))));
        pior.filter(e -> e.roiPercentual().signum() < 0 && !e.equals(melhor.orElse(null))).ifPresent(e -> {
            insights.add("A estratégia \"%s\" está com ROI negativo (%s).".formatted(e.titulo(),
                    percentual(e.roiPercentual())));
            recomendacoes.add("Revise o portfólio da estratégia \"%s\": ajuste escopo, prazo ou investimento.".formatted(e.titulo()));
        });
    }

    private static void analisarIdeias(ResumoIdeiasResponse ideias, List<String> insights,
            List<String> recomendacoes) {
        if (ideias.emAnalise() > 0) {
            insights.add("%d ideia(s) aguardam análise dos gestores.".formatted(ideias.emAnalise()));
            recomendacoes.add("Use a pontuação por IA para priorizar a fila de ideias e responder aos autores.");
        }
        if (ideias.taxaAprovacaoPercentual() != null) {
            insights.add("A taxa de aprovação das ideias decididas é de %s."
                    .formatted(percentual(ideias.taxaAprovacaoPercentual())));
        }
    }

    private static void analisarProjetosSemResultado(List<DesempenhoProjetoResponse> projetos,
            List<String> insights, List<String> recomendacoes) {
        long semResultado = projetos.stream()
                .filter(p -> p.status() == StatusProjeto.EM_ANDAMENTO || p.status() == StatusProjeto.CONCLUIDO)
                .filter(p -> p.retorno().signum() == 0)
                .count();
        if (semResultado > 0) {
            insights.add("%d projeto(s) em andamento ou concluídos ainda não registraram resultados.".formatted(semResultado));
            recomendacoes.add("Peça aos gestores que registrem os resultados para que o ROI reflita a realidade.");
        }
    }

    private static String moeda(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(valor);
    }

    private static String percentual(BigDecimal valor) {
        return Numeros.percentual(valor);
    }
}
