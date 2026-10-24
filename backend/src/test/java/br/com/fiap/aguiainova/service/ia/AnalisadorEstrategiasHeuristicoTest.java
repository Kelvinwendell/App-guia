package br.com.fiap.aguiainova.service.ia;

import java.time.LocalDate;
import java.util.List;

import br.com.fiap.aguiainova.model.DadosEstrategia;
import br.com.fiap.aguiainova.model.Estrategia;
import br.com.fiap.aguiainova.support.Dados;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnalisadorEstrategiasHeuristicoTest {

    private static final String DESCRICAO_COMPLETA = "Digitalizar o chão de fábrica com sensores e painéis, medindo "
            + "as paradas por turno para que a equipe acompanhe a evolução semana a semana e decida com dados.";
    private static final LocalDate INICIO = LocalDate.of(2026, 11, 1);
    private static final LocalDate FIM = LocalDate.of(2027, 4, 30);

    private final AnalisadorEstrategiasHeuristico analisador = new AnalisadorEstrategiasHeuristico();

    private static DadosEstrategia dados(String titulo, String descricao, List<String> metas, LocalDate inicio,
            LocalDate fim) {
        return new DadosEstrategia(titulo, descricao, "Tecnologia", null, null, metas, inicio, fim);
    }

    @Test
    void estrategiaCompletaEMensuravelTiraNotaMaximaSemMelhorias() {
        AnaliseGerada analise = analisador.analisar(dados("Fábrica conectada e orientada por dados",
                DESCRICAO_COMPLETA, List.of("Monitorar 100% das máquinas da linha 3", "Reduzir paradas em 20% até abril"),
                INICIO, FIM), List.of());

        assertThat(analise.nota()).isEqualTo(100);
        assertThat(analise.melhorias()).isEmpty();
        assertThat(analise.pontosFortes()).contains("Descrição detalhada", "2 meta(s) definida(s)",
                "Metas com número ou prazo", "Período de vigência definido");
        assertThat(analise.fonte()).isEqualTo(FonteIa.HEURISTICA);
        assertThat(analise.resumo()).contains("nota 100/100").contains("bem estruturada");
    }

    @Test
    void estrategiaVaziaDeConteudoRecebeNotaBaixaEMelhoriasParaCadaLacuna() {
        AnaliseGerada analise = analisador.analisar(dados("Crescer", "Crescer mais.", List.of(), null, null),
                List.of());

        assertThat(analise.nota()).isZero();
        assertThat(analise.melhorias()).hasSize(4)
                .anyMatch(m -> m.contains("Descrição muito curta"))
                .anyMatch(m -> m.contains("Título muito curto"))
                .anyMatch(m -> m.contains("2 a 5 metas"))
                .anyMatch(m -> m.contains("período de vigência"));
    }

    @Test
    void metasSemNumeroValemMenosQueMetasMensuraveis() {
        AnaliseGerada analise = analisador.analisar(dados("Fábrica conectada e orientada por dados",
                DESCRICAO_COMPLETA, List.of("Melhorar a comunicação"), INICIO, FIM), List.of());

        assertThat(analise.nota()).isEqualTo(75);
        assertThat(analise.melhorias()).containsExactly("Torne as metas mensuráveis: inclua número, prazo ou indicador.");
    }

    @Test
    void periodoIncompletoPontuaMenosEPedeOQueFalta() {
        AnaliseGerada analise = analisador.analisar(dados("Fábrica conectada e orientada por dados",
                DESCRICAO_COMPLETA, List.of("Reduzir paradas em 20%"), INICIO, null), List.of());

        assertThat(analise.nota()).isEqualTo(90);
        assertThat(analise.melhorias()).containsExactly("Informe início e fim do período para acompanhar o prazo.");
    }

    @Test
    void detectaSobreposicaoComEstrategiaVigenteEDescontaNota() {
        Estrategia parecida = Dados.estrategia("e-1", "Transformação digital do chão de fábrica",
                "Digitalizar processos e monitorar máquinas em tempo real.", "Tecnologia", null);
        Estrategia diferente = Dados.estrategia("e-2", "Segurança e bem-estar do colaborador",
                "Reduzir acidentes e afastamentos com ergonomia e prevenção.", "Pessoas", null);

        AnaliseGerada analise = analisador.analisar(dados("Fábrica digital com sensores", DESCRICAO_COMPLETA,
                List.of("Reduzir paradas em 20%"), INICIO, FIM), List.of(parecida, diferente));

        assertThat(analise.sobreposicoes()).containsExactly("e-1");
        assertThat(analise.nota()).isEqualTo(90);
        assertThat(analise.melhorias()).containsExactly(
                "Possível sobreposição com \"Transformação digital do chão de fábrica\": confirme se não é a mesma diretriz.");
    }

    @Test
    void naoPenalizaMaisDeDuasSobreposicoes() {
        List<Estrategia> parecidas = List.of(
                Dados.estrategia("e-1", "Fábrica digital", DESCRICAO_COMPLETA, "Tecnologia", null),
                Dados.estrategia("e-2", "Fábrica conectada", DESCRICAO_COMPLETA, "Tecnologia", null),
                Dados.estrategia("e-3", "Fábrica inteligente", DESCRICAO_COMPLETA, "Tecnologia", null));

        AnaliseGerada analise = analisador.analisar(dados("Fábrica orientada por dados", DESCRICAO_COMPLETA,
                List.of("Reduzir paradas em 20%"), INICIO, FIM), parecidas);

        assertThat(analise.sobreposicoes()).hasSize(3);
        assertThat(analise.nota()).isEqualTo(80);
    }
}
