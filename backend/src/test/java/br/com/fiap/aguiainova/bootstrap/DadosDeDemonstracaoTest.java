package br.com.fiap.aguiainova.bootstrap;

import java.time.Clock;

import br.com.fiap.aguiainova.config.AppProperties;
import br.com.fiap.aguiainova.repository.UsuarioRepository;
import br.com.fiap.aguiainova.service.EstrategiaService;
import br.com.fiap.aguiainova.service.IdeiaService;
import br.com.fiap.aguiainova.service.ProjetoService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/** O que acontece (e, principalmente, o que NAO acontece) conforme o modo e a senha configurados. */
class DadosDeDemonstracaoTest {

    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final EstrategiaService estrategias = mock(EstrategiaService.class);
    private final IdeiaService ideias = mock(IdeiaService.class);
    private final ProjetoService projetos = mock(ProjetoService.class);
    private final MongoTemplate mongo = mock(MongoTemplate.class);

    private DadosDeDemonstracao comConfiguracao(String modo, String senha) {
        AppProperties props = mock(AppProperties.class);
        when(props.seed()).thenReturn(new AppProperties.Seed(false, new AppProperties.Seed.Demo(modo, senha)));
        return new DadosDeDemonstracao(usuarios, encoder, estrategias, ideias, projetos, mongo, Clock.systemUTC(),
                props);
    }

    private void verificarQueNadaFoiTocado() {
        verifyNoInteractions(usuarios, encoder, estrategias, ideias, projetos, mongo);
    }

    @Test
    void desligadoPorPadraoNaoFazNada() {
        comConfiguracao("desligado", "").run(new DefaultApplicationArguments());

        verificarQueNadaFoiTocado();
    }

    @Test
    void semModoDefinidoNaoFazNada() {
        comConfiguracao(null, null).run(new DefaultApplicationArguments());

        verificarQueNadaFoiTocado();
    }

    @Test
    void carregarSemSenhaNaoCriaNada() {
        comConfiguracao("carregar", "").run(new DefaultApplicationArguments());

        verificarQueNadaFoiTocado();
    }

    @Test
    void carregarComSenhaCurtaNaoCriaNada() {
        comConfiguracao("carregar", "12345").run(new DefaultApplicationArguments());

        verificarQueNadaFoiTocado();
    }

    @Test
    void carregarComSenhaSoDeEspacosNaoCriaNada() {
        comConfiguracao("carregar", "          ").run(new DefaultApplicationArguments());

        verificarQueNadaFoiTocado();
    }

    @Test
    void carregarQuandoJaExisteNaoRecriaNada() {
        when(usuarios.existsByEmail(RoteiroDemo.EMAIL_DA_LIDERANCA)).thenReturn(true);

        comConfiguracao("carregar", "senha-forte-123").run(new DefaultApplicationArguments());

        verifyNoInteractions(encoder, estrategias, ideias, projetos, mongo);
        verify(usuarios).existsByEmail(RoteiroDemo.EMAIL_DA_LIDERANCA);
        verifyNoMoreInteractions(usuarios);
    }

    @Test
    void removerSemContasDeDemonstracaoNaoApagaNada() {
        comConfiguracao("remover", "").run(new DefaultApplicationArguments());

        verifyNoInteractions(estrategias, ideias, projetos, mongo);
    }
}
