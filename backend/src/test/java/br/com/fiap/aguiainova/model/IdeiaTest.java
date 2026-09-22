package br.com.fiap.aguiainova.model;

import java.time.Instant;
import java.util.List;

import br.com.fiap.aguiainova.exception.ConflitoException;
import br.com.fiap.aguiainova.exception.RegraDeNegocioException;
import br.com.fiap.aguiainova.exception.RequisicaoInvalidaException;
import br.com.fiap.aguiainova.model.enums.Prioridade;
import br.com.fiap.aguiainova.model.enums.StatusIdeia;
import br.com.fiap.aguiainova.support.Dados;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdeiaTest {

    private static final Autor GESTOR = new Autor("g-1", "Gestor");
    private static final Instant AGORA = Instant.parse("2026-06-15T12:00:00Z");

    private Ideia ideia() {
        return Dados.ideia("i-1", "  Título  ", " Descrição ", "Tecnologia", "  ");
    }

    private static DadosIdeia dados(String titulo) {
        return new DadosIdeia(titulo, "Nova descrição", "Tecnologia", null);
    }

    @Test
    void nasceEmAnaliseComPrioridadeMediaENormalizaOsTextos() {
        Ideia ideia = ideia();

        assertThat(ideia.getStatus()).isEqualTo(StatusIdeia.EM_ANALISE);
        assertThat(ideia.getPrioridade()).isEqualTo(Prioridade.MEDIA);
        assertThat(ideia.getTitulo()).isEqualTo("Título");
        assertThat(ideia.getDescricao()).isEqualTo("Descrição");
        assertThat(ideia.getEstrategiaId()).isNull();
        assertThat(ideia.getAutor()).isEqualTo(Dados.AUTOR);
    }

    @Test
    void oConteudoObrigatorioNuncaFicaEmBranco() {
        assertThatThrownBy(() -> new DadosIdeia(" ", "d", "c", null)).isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> new DadosIdeia("t", null, "c", null)).isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> new DadosIdeia("t", "d", "", null)).isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void oAutorRevisaEnquantoEmAnaliseEANotaDaIaAntigaEDescartada() {
        Ideia ideia = ideia();
        ideia.registrarAvaliacaoIa(new AvaliacaoIa(80, 70, Prioridade.ALTA, "ok", List.of(), List.of(), "GEMINI", AGORA));

        ideia.revisar(dados("Título novo"));

        assertThat(ideia.getTitulo()).isEqualTo("Título novo");
        assertThat(ideia.getAvaliacaoIa()).isNull();
    }

    @Test
    void depoisDeAvaliadaNaoPodeSerRevisadaNemExcluida() {
        Ideia ideia = ideia();
        ideia.avaliar(StatusIdeia.APROVADA, null, null, GESTOR, AGORA);

        assertThatThrownBy(() -> ideia.revisar(dados("Tentativa"))).isInstanceOf(ConflitoException.class);
        assertThatThrownBy(ideia::validarRemocao).isInstanceOf(ConflitoException.class);
    }

    @Test
    void avaliarExigeAoMenosUmaDecisao() {
        assertThatThrownBy(() -> ideia().avaliar(null, null, "  ", GESTOR, AGORA))
                .isInstanceOf(RequisicaoInvalidaException.class);
    }

    @Test
    void avaliarAplicaSoOQueFoiInformadoERegistraQuemDecidiu() {
        Ideia ideia = ideia();

        ideia.avaliar(null, Prioridade.ALTA, null, GESTOR, AGORA);
        assertThat(ideia.getStatus()).isEqualTo(StatusIdeia.EM_ANALISE);
        assertThat(ideia.getPrioridade()).isEqualTo(Prioridade.ALTA);

        ideia.avaliar(StatusIdeia.REJEITADA, null, "  Sem verba  ", GESTOR, AGORA);
        assertThat(ideia.getStatus()).isEqualTo(StatusIdeia.REJEITADA);
        assertThat(ideia.getPrioridade()).isEqualTo(Prioridade.ALTA);
        assertThat(ideia.getParecer()).isEqualTo("Sem verba");
        assertThat(ideia.getAvaliador()).isEqualTo(GESTOR);
        assertThat(ideia.getAvaliadaEm()).isEqualTo(AGORA);
    }

    @Test
    void identificaSeUmaIdeiaPertenceAUmUsuario() {
        Ideia ideia = ideia();

        assertThat(ideia.pertenceA(Dados.AUTOR.usuarioId())).isTrue();
        assertThat(ideia.pertenceA("outro")).isFalse();
        assertThat(ideia.pertenceA(null)).isFalse();
    }
}
