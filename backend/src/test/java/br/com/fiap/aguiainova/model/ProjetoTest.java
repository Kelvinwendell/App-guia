package br.com.fiap.aguiainova.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.com.fiap.aguiainova.exception.NaoEncontradoException;
import br.com.fiap.aguiainova.exception.RegraDeNegocioException;
import br.com.fiap.aguiainova.exception.RequisicaoInvalidaException;
import br.com.fiap.aguiainova.model.enums.EtapaProjeto;
import br.com.fiap.aguiainova.model.enums.StatusProjeto;
import br.com.fiap.aguiainova.support.Dados;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProjetoTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 6, 15);

    private Projeto projeto(StatusProjeto status, LocalDate prazo) {
        return Dados.projeto(new BigDecimal("1000"), HOJE.minusDays(30), prazo, status);
    }

    private DadosResultado resultado(String retorno) {
        return new DadosResultado(HOJE, "Marco", new BigDecimal(retorno), 5.0, null);
    }

    @Test
    void nasceEmPlanejamentoNaEtapaDeIniciacaoSemProgresso() {
        Projeto projeto = projeto(null, HOJE.plusDays(10));

        assertThat(projeto.getStatus()).isEqualTo(StatusProjeto.PLANEJAMENTO);
        assertThat(projeto.getEtapa()).isEqualTo(EtapaProjeto.INICIACAO);
        assertThat(projeto.getProgresso()).isZero();
        assertThat(projeto.getResultados()).isEmpty();
    }

    @Test
    void osDadosCadastraisNuncaSaoInvalidos() {
        assertThatThrownBy(() -> Dados.dadosProjeto(new BigDecimal("100"), HOJE, HOJE.minusDays(1), null))
                .isInstanceOf(RegraDeNegocioException.class).hasMessageContaining("prazo");
        assertThatThrownBy(() -> Dados.dadosProjeto(new BigDecimal("-1"), HOJE, HOJE, null))
                .isInstanceOf(RegraDeNegocioException.class).hasMessageContaining("investimento");
        assertThatThrownBy(() -> new DadosProjeto(" ", "d", BigDecimal.ONE, HOJE, HOJE, null, null, null, null))
                .isInstanceOf(RegraDeNegocioException.class).hasMessageContaining("nome");
    }

    @Test
    void progressoPrecisaEstarEntreZeroECem() {
        Projeto projeto = projeto(null, HOJE.plusDays(10));

        assertThatThrownBy(() -> projeto.atualizarAndamento(null, null, 101)).isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> projeto.atualizarAndamento(null, null, -1)).isInstanceOf(RegraDeNegocioException.class);
        projeto.atualizarAndamento(null, null, 100);
        assertThat(projeto.getProgresso()).isEqualTo(100);
    }

    @Test
    void concluirUmProjetoForcaProgressoDeCemPorCento() {
        Projeto projeto = projeto(StatusProjeto.EM_ANDAMENTO, HOJE.plusDays(10));
        projeto.atualizarAndamento(null, null, 30);

        projeto.atualizarAndamento(StatusProjeto.CONCLUIDO, null, 30);

        assertThat(projeto.getProgresso()).isEqualTo(100);
    }

    @Test
    void atualizarAndamentoExigeAoMenosUmCampoEPreservaOsOmitidos() {
        Projeto projeto = projeto(StatusProjeto.EM_ANDAMENTO, HOJE.plusDays(10));
        assertThatThrownBy(() -> projeto.atualizarAndamento(null, null, null))
                .isInstanceOf(RequisicaoInvalidaException.class);

        projeto.atualizarAndamento(null, EtapaProjeto.EXECUCAO, null);

        assertThat(projeto.getStatus()).isEqualTo(StatusProjeto.EM_ANDAMENTO);
        assertThat(projeto.getEtapa()).isEqualTo(EtapaProjeto.EXECUCAO);
    }

    @Test
    void atualizarDadosMantemStatusEEtapaQuandoNaoInformados() {
        Projeto projeto = projeto(StatusProjeto.EM_ANDAMENTO, HOJE.plusDays(10));
        projeto.atualizarAndamento(null, EtapaProjeto.EXECUCAO, null);

        projeto.atualizarDados(Dados.dadosProjeto(new BigDecimal("2000"), HOJE, HOJE.plusDays(20), null));

        assertThat(projeto.getInvestimento()).isEqualByComparingTo("2000");
        assertThat(projeto.getStatus()).isEqualTo(StatusProjeto.EM_ANDAMENTO);
        assertThat(projeto.getEtapa()).isEqualTo(EtapaProjeto.EXECUCAO);
    }

    @Test
    void osResultadosSoMudamPelosMetodosDoProjeto() {
        Projeto projeto = projeto(null, HOJE.plusDays(10));
        projeto.registrarResultado(resultado("100"), Dados.AUTOR);
        projeto.registrarResultado(resultado("200"), Dados.AUTOR);

        assertThat(projeto.getResultados()).hasSize(2);
        assertThat(projeto.getResultados()).extracting(ResultadoProjeto::id).doesNotHaveDuplicates().doesNotContainNull();
        assertThatThrownBy(() -> projeto.getResultados().clear()).isInstanceOf(UnsupportedOperationException.class);

        String id = projeto.getResultados().get(0).id();
        projeto.removerResultado(id);

        assertThat(projeto.getResultados()).hasSize(1);
        assertThatThrownBy(() -> projeto.removerResultado(id)).isInstanceOf(NaoEncontradoException.class);
    }

    @Test
    void resultadoNaoAceitaRetornoNegativo() {
        assertThatThrownBy(() -> resultado("-1")).isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void soEstaAtrasadoQuemAindaEstaEmAbertoComOPrazoVencido() {
        assertThat(projeto(StatusProjeto.EM_ANDAMENTO, HOJE.minusDays(1)).estaAtrasado(HOJE)).isTrue();
        assertThat(projeto(StatusProjeto.PAUSADO, HOJE.minusDays(1)).estaAtrasado(HOJE)).isTrue();
        assertThat(projeto(StatusProjeto.EM_ANDAMENTO, HOJE).estaAtrasado(HOJE)).isFalse();
        assertThat(projeto(StatusProjeto.CONCLUIDO, HOJE.minusDays(1)).estaAtrasado(HOJE)).isFalse();
        assertThat(projeto(StatusProjeto.CANCELADO, HOJE.minusDays(1)).estaAtrasado(HOJE)).isFalse();
    }

    @Test
    void calculaOsDiasAteOPrazoIncluindoNegativos() {
        assertThat(projeto(null, HOJE.plusDays(10)).diasParaPrazo(HOJE)).isEqualTo(10);
        assertThat(projeto(null, HOJE.minusDays(3)).diasParaPrazo(HOJE)).isEqualTo(-3);
    }
}
