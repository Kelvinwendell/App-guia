package br.com.fiap.aguiainova.support;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.com.fiap.aguiainova.config.AppProperties;
import br.com.fiap.aguiainova.model.Autor;
import br.com.fiap.aguiainova.model.DadosEstrategia;
import br.com.fiap.aguiainova.model.DadosIdeia;
import br.com.fiap.aguiainova.model.DadosProjeto;
import br.com.fiap.aguiainova.model.Estrategia;
import br.com.fiap.aguiainova.model.Ideia;
import br.com.fiap.aguiainova.model.Projeto;
import br.com.fiap.aguiainova.model.enums.StatusProjeto;
import org.springframework.test.util.ReflectionTestUtils;

/** Fabricas de objetos de dominio para testes unitarios (sem banco). */
public final class Dados {

    public static final Autor AUTOR = new Autor("u-1", "Autor Teste");
    public static final String SEGREDO_JWT = "segredo-de-teste-com-mais-de-32-bytes!!";

    private Dados() {
    }

    /** Os ids sao gerados pelo banco; nos testes unitarios eles sao atribuidos por reflexao. */
    public static <T> T comId(T documento, String id) {
        ReflectionTestUtils.setField(documento, "id", id);
        return documento;
    }

    public static Ideia ideia(String id, String titulo, String descricao, String categoria, String estrategiaId) {
        return comId(new Ideia(new DadosIdeia(titulo, descricao, categoria, estrategiaId), AUTOR), id);
    }

    public static Estrategia estrategia(String id, String titulo, String descricao, String categoria,
            String campanha) {
        return comId(new Estrategia(new DadosEstrategia(titulo, descricao, categoria, campanha, null), AUTOR), id);
    }

    public static DadosProjeto dadosProjeto(BigDecimal investimento, LocalDate inicio, LocalDate prazo,
            StatusProjeto status) {
        return new DadosProjeto("Projeto", "Descrição", investimento, inicio, prazo, null, null, status, null);
    }

    public static Projeto projeto(BigDecimal investimento, LocalDate inicio, LocalDate prazo, StatusProjeto status) {
        return new Projeto(dadosProjeto(investimento, inicio, prazo, status), AUTOR);
    }

    public static AppProperties propriedades(boolean permitirEscolhaDePerfil, boolean iaHabilitada, String chaveIa,
            String provedorIa) {
        return new AppProperties(
                new AppProperties.Jwt(SEGREDO_JWT, 60, "aguia-inova-api"),
                new AppProperties.Security(permitirEscolhaDePerfil),
                new AppProperties.Seed(false),
                new AppProperties.Ai(iaHabilitada, provedorIa, chaveIa, "modelo-teste", "", 5),
                new AppProperties.RateLimit(1000, 1000),
                new AppProperties.Dashboard(5000));
    }
}
