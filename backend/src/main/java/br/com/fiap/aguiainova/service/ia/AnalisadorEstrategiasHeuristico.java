package br.com.fiap.aguiainova.service.ia;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import br.com.fiap.aguiainova.model.DadosEstrategia;
import br.com.fiap.aguiainova.model.Estrategia;
import org.springframework.stereotype.Component;

/**
 * Critica por regras, deterministica e sem rede: plano de contingencia para quando a IA nao esta disponivel. A nota
 * soma descricao (ate 25), titulo (10), metas (20, mais 25 se alguma tem numero ou prazo) e periodo (20), e perde 10
 * pontos por sobreposicao com outra estrategia vigente (no maximo 20). Marcada com a fonte HEURISTICA.
 */
@Component
public class AnalisadorEstrategiasHeuristico implements AnalisadorDeEstrategias {

    private static final int DESCRICAO_COMPLETA = 120;
    private static final int DESCRICAO_MINIMA = 60;
    private static final int TITULO_MINIMO = 10;
    private static final int TITULO_MAXIMO = 100;
    private static final int PONTOS_DESCRICAO_COMPLETA = 25;
    private static final int PONTOS_DESCRICAO_MINIMA = 10;
    private static final int PONTOS_TITULO = 10;
    private static final int PONTOS_METAS = 20;
    private static final int PONTOS_METAS_MENSURAVEIS = 25;
    private static final int PONTOS_PERIODO = 20;
    private static final int PONTOS_PERIODO_PARCIAL = 10;
    private static final int PENALIDADE_SOBREPOSICAO = 10;
    private static final int MAXIMO_SOBREPOSICOES_PENALIZADAS = 2;
    private static final double AFINIDADE_SOBREPOSTA = 0.5;
    private static final double AFINIDADE_SOBREPOSTA_MESMA_CATEGORIA = 0.3;
    private static final Pattern TEM_NUMERO = Pattern.compile("\\d");

    @Override
    public AnaliseGerada analisar(DadosEstrategia dados, List<Estrategia> outrasVigentes) {
        List<String> fortes = new ArrayList<>();
        List<String> melhorias = new ArrayList<>();
        int nota = 0;
        nota += pontosDaDescricao(dados, fortes, melhorias);
        nota += pontosDoTitulo(dados, melhorias);
        nota += pontosDasMetas(dados, fortes, melhorias);
        nota += pontosDoPeriodo(dados, fortes, melhorias);

        List<Estrategia> sobrepostas = sobrepostas(dados, outrasVigentes);
        sobrepostas.forEach(outra -> melhorias.add(
                "Possível sobreposição com \"%s\": confirme se não é a mesma diretriz.".formatted(outra.getTitulo())));
        nota = Math.max(0, nota - PENALIDADE_SOBREPOSICAO * Math.min(MAXIMO_SOBREPOSICOES_PENALIZADAS,
                sobrepostas.size()));

        String resumo = "Análise por regras (IA indisponível): nota %d/100. %s".formatted(nota, melhorias.isEmpty()
                ? "A estratégia está bem estruturada."
                : "%d ponto(s) a melhorar antes de publicar.".formatted(melhorias.size()));
        return new AnaliseGerada(nota, resumo, fortes, melhorias, sobrepostas.stream().map(Estrategia::getId).toList(),
                FonteIa.HEURISTICA);
    }

    private static int pontosDaDescricao(DadosEstrategia dados, List<String> fortes, List<String> melhorias) {
        int tamanho = dados.descricao().length();
        if (tamanho >= DESCRICAO_COMPLETA) {
            fortes.add("Descrição detalhada");
            return PONTOS_DESCRICAO_COMPLETA;
        }
        melhorias.add(tamanho >= DESCRICAO_MINIMA
                ? "Detalhe a descrição: o que muda, por quê e como será medido."
                : "Descrição muito curta: explique o objetivo, o motivo e como o avanço será medido.");
        return tamanho >= DESCRICAO_MINIMA ? PONTOS_DESCRICAO_MINIMA : 0;
    }

    private static int pontosDoTitulo(DadosEstrategia dados, List<String> melhorias) {
        int tamanho = dados.titulo().length();
        if (tamanho >= TITULO_MINIMO && tamanho <= TITULO_MAXIMO) {
            return PONTOS_TITULO;
        }
        melhorias.add(tamanho < TITULO_MINIMO
                ? "Título muito curto: diga qual é a diretriz."
                : "Título longo: resuma a diretriz em uma frase curta.");
        return 0;
    }

    private static int pontosDasMetas(DadosEstrategia dados, List<String> fortes, List<String> melhorias) {
        if (dados.metas().isEmpty()) {
            melhorias.add("Defina de 2 a 5 metas para saber se a estratégia está funcionando.");
            return 0;
        }
        fortes.add("%d meta(s) definida(s)".formatted(dados.metas().size()));
        if (dados.metas().stream().anyMatch(meta -> TEM_NUMERO.matcher(meta).find())) {
            fortes.add("Metas com número ou prazo");
            return PONTOS_METAS + PONTOS_METAS_MENSURAVEIS;
        }
        melhorias.add("Torne as metas mensuráveis: inclua número, prazo ou indicador.");
        return PONTOS_METAS;
    }

    private static int pontosDoPeriodo(DadosEstrategia dados, List<String> fortes, List<String> melhorias) {
        if (dados.inicio() != null && dados.fim() != null) {
            fortes.add("Período de vigência definido");
            return PONTOS_PERIODO;
        }
        melhorias.add(dados.inicio() == null && dados.fim() == null
                ? "Defina o período de vigência (início e fim)."
                : "Informe início e fim do período para acompanhar o prazo.");
        return dados.inicio() == null && dados.fim() == null ? 0 : PONTOS_PERIODO_PARCIAL;
    }

    private static List<Estrategia> sobrepostas(DadosEstrategia dados, List<Estrategia> outras) {
        Set<String> palavras = PalavrasChave.de(dados.titulo(), dados.descricao(), dados.categoria());
        return outras.stream().filter(outra -> {
            double afinidade = PalavrasChave.afinidade(palavras,
                    PalavrasChave.de(outra.getTitulo(), outra.getDescricao(), outra.getCategoria()));
            boolean mesmaCategoria = dados.categoria().equalsIgnoreCase(outra.getCategoria());
            return afinidade >= AFINIDADE_SOBREPOSTA
                    || (mesmaCategoria && afinidade >= AFINIDADE_SOBREPOSTA_MESMA_CATEGORIA);
        }).toList();
    }
}
