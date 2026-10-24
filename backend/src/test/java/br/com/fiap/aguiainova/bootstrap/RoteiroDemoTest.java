package br.com.fiap.aguiainova.bootstrap;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import br.com.fiap.aguiainova.bootstrap.RoteiroDemo.EstrategiaDemo;
import br.com.fiap.aguiainova.bootstrap.RoteiroDemo.IdeiaDemo;
import br.com.fiap.aguiainova.bootstrap.RoteiroDemo.ProjetoDemo;
import br.com.fiap.aguiainova.bootstrap.RoteiroDemo.ResultadoDemo;
import br.com.fiap.aguiainova.bootstrap.RoteiroDemo.UsuarioDemo;
import br.com.fiap.aguiainova.model.enums.Perfil;
import br.com.fiap.aguiainova.model.enums.StatusEstrategia;
import br.com.fiap.aguiainova.model.enums.StatusIdeia;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** O roteiro e so dados; estes testes garantem que ele e coerente antes de qualquer banco entrar em cena. */
class RoteiroDemoTest {

    private final Map<String, EstrategiaDemo> estrategias = RoteiroDemo.estrategias().stream()
            .collect(Collectors.toMap(EstrategiaDemo::chave, Function.identity()));
    private final Map<String, IdeiaDemo> ideias = RoteiroDemo.ideias().stream()
            .collect(Collectors.toMap(IdeiaDemo::chave, Function.identity()));
    private final Map<String, UsuarioDemo> usuarios = RoteiroDemo.usuarios().stream()
            .collect(Collectors.toMap(UsuarioDemo::chave, Function.identity()));

    @Test
    void temUmaLiderancaUmGestorEOperadoresComEmailsUnicosNoDominioFicticio() {
        List<UsuarioDemo> contas = RoteiroDemo.usuarios();

        assertThat(contas).extracting(UsuarioDemo::perfil)
                .containsOnlyOnce(Perfil.LIDERANCA, Perfil.GESTOR)
                .contains(Perfil.OPERADOR);
        assertThat(contas).extracting(UsuarioDemo::email).doesNotHaveDuplicates()
                .allMatch(email -> email.endsWith("@aguiainova.demo"));
        assertThat(contas).extracting(UsuarioDemo::chave).doesNotHaveDuplicates();
        assertThat(RoteiroDemo.emails()).hasSameSizeAs(contas).contains(RoteiroDemo.EMAIL_DA_LIDERANCA);
    }

    @Test
    void aContaQueMarcaOCarregamentoEALideranca() {
        assertThat(usuarios.get("lideranca").email()).isEqualTo(RoteiroDemo.EMAIL_DA_LIDERANCA);
        assertThat(usuarios.get("lideranca").perfil()).isEqualTo(Perfil.LIDERANCA);
        assertThat(usuarios.get("gestor").perfil()).isEqualTo(Perfil.GESTOR);
    }

    @Test
    void chavesSaoUnicasEmCadaLista() {
        assertThat(RoteiroDemo.estrategias()).extracting(EstrategiaDemo::chave).doesNotHaveDuplicates();
        assertThat(RoteiroDemo.ideias()).extracting(IdeiaDemo::chave).doesNotHaveDuplicates();
        assertThat(RoteiroDemo.projetos()).extracting(ProjetoDemo::nome).doesNotHaveDuplicates();
    }

    @Test
    void periodosDasEstrategiasComecamAntesDeTerminar() {
        assertThat(RoteiroDemo.estrategias()).allSatisfy(e -> assertThat(e.inicioEmDias()).isLessThan(e.fimEmDias()));
    }

    @Test
    void soUmaEstrategiaEstaEncerradaENadaSeVinculaAEla() {
        assertThat(RoteiroDemo.estrategias()).filteredOn(e -> e.status() == StatusEstrategia.ENCERRADA)
                .extracting(EstrategiaDemo::chave).containsExactly("online");
        assertThat(RoteiroDemo.ideias()).extracting(IdeiaDemo::estrategia).doesNotContain("online");
        assertThat(RoteiroDemo.projetos()).extracting(ProjetoDemo::estrategia).doesNotContain("online");
    }

    @Test
    void ideiasPartemDeOperadoresEReferenciamEstrategiasDoRoteiro() {
        assertThat(RoteiroDemo.ideias()).allSatisfy(ideia -> {
            assertThat(usuarios).containsKey(ideia.autor());
            assertThat(usuarios.get(ideia.autor()).perfil()).isEqualTo(Perfil.OPERADOR);
            if (ideia.estrategia() != null) {
                assertThat(estrategias).containsKey(ideia.estrategia());
            }
        });
    }

    @Test
    void soAsIdeiasJaDecididasTemPrioridadeEParecer() {
        assertThat(RoteiroDemo.ideias()).allSatisfy(ideia -> {
            if (ideia.decisao() == StatusIdeia.EM_ANALISE) {
                assertThat(ideia.prioridade()).isNull();
                assertThat(ideia.parecer()).isNull();
            } else {
                assertThat(ideia.prioridade()).isNotNull();
                assertThat(ideia.parecer()).isNotBlank();
            }
        });
    }

    @Test
    void mostraIdeiasEmTodosOsEstados() {
        assertThat(RoteiroDemo.ideias()).extracting(IdeiaDemo::decisao)
                .contains(StatusIdeia.APROVADA, StatusIdeia.REJEITADA, StatusIdeia.EM_ANALISE);
    }

    @Test
    void cadaProjetoNasceDeUmaIdeiaAprovadaDaMesmaEstrategiaEUmaIdeiaViraNoMaximoUmProjeto() {
        Set<String> usadas = new HashSet<>();
        assertThat(RoteiroDemo.projetos()).allSatisfy(projeto -> {
            assertThat(estrategias).containsKey(projeto.estrategia());
            assertThat(projeto.inicioEmDias()).isLessThan(projeto.prazoEmDias());
            assertThat(ideias).containsKey(projeto.ideia());
            IdeiaDemo origem = ideias.get(projeto.ideia());
            assertThat(origem.decisao()).isEqualTo(StatusIdeia.APROVADA);
            assertThat(origem.estrategia()).isEqualTo(projeto.estrategia());
            assertThat(usadas.add(projeto.ideia())).as("ideia %s usada duas vezes", projeto.ideia()).isTrue();
        });
    }

    @Test
    void resultadosSaoDoPassadoComDescricaoERetornoPositivo() {
        List<ResultadoDemo> resultados = RoteiroDemo.projetos().stream().flatMap(p -> p.resultados().stream()).toList();

        assertThat(resultados).isNotEmpty().allSatisfy(resultado -> {
            assertThat(resultado.diasAtras()).isPositive();
            assertThat(resultado.descricao()).isNotBlank();
            assertThat(Double.parseDouble(resultado.retorno())).isPositive();
        });
    }
}
