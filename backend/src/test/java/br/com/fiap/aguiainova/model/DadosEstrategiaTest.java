package br.com.fiap.aguiainova.model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import br.com.fiap.aguiainova.exception.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DadosEstrategiaTest {

    private static DadosEstrategia dados(List<String> metas, LocalDate inicio, LocalDate fim) {
        return new DadosEstrategia(" Título ", "Descrição", "Geral", " ", null, metas, inicio, fim);
    }

    @Test
    void normalizaTextosEDescartaMetasEmBranco() {
        DadosEstrategia dados = dados(java.util.Arrays.asList("  Reduzir 10%  ", "", "   ", null, "Outra"), null, null);

        assertThat(dados.titulo()).isEqualTo("Título");
        assertThat(dados.campanha()).isNull();
        assertThat(dados.metas()).containsExactly("Reduzir 10%", "Outra");
    }

    @Test
    void metasAusentesViramListaVazia() {
        assertThat(dados(null, null, null).metas()).isEmpty();
        assertThat(new DadosEstrategia("T", "D", "C", null, null).metas()).isEmpty();
    }

    @Test
    void limitaAQuantidadeDeMetas() {
        List<String> nove = IntStream.rangeClosed(1, DadosEstrategia.MAXIMO_METAS + 1).mapToObj(i -> "Meta " + i).toList();

        assertThatThrownBy(() -> dados(nove, null, null)).isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("no máximo 8 metas");
        assertThat(dados(nove.subList(0, DadosEstrategia.MAXIMO_METAS), null, null).metas()).hasSize(8);
    }

    @Test
    void fimNaoPodeSerAnteriorAoInicio() {
        LocalDate inicio = LocalDate.of(2026, 5, 10);

        assertThatThrownBy(() -> dados(Collections.emptyList(), inicio, inicio.minusDays(1)))
                .isInstanceOf(RegraDeNegocioException.class).hasMessageContaining("fim");
        assertThat(dados(Collections.emptyList(), inicio, inicio).fim()).isEqualTo(inicio);
    }

    @Test
    void periodoPodeTerSoUmDosLados() {
        assertThat(dados(List.of(), LocalDate.of(2026, 1, 1), null).fim()).isNull();
        assertThat(dados(List.of(), null, LocalDate.of(2026, 12, 31)).inicio()).isNull();
    }
}
