package br.com.fiap.aguiainova.service;

import java.util.Optional;

import br.com.fiap.aguiainova.dto.request.RegistroRequest;
import br.com.fiap.aguiainova.dto.response.UsuarioResponse;
import br.com.fiap.aguiainova.exception.ConflitoException;
import br.com.fiap.aguiainova.exception.RegraDeNegocioException;
import br.com.fiap.aguiainova.mapper.UsuarioMapper;
import br.com.fiap.aguiainova.model.DadosUsuario;
import br.com.fiap.aguiainova.model.Usuario;
import br.com.fiap.aguiainova.model.enums.Perfil;
import br.com.fiap.aguiainova.repository.UsuarioRepository;
import br.com.fiap.aguiainova.security.UsuarioLogado;
import br.com.fiap.aguiainova.support.Dados;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarios;
    @Mock
    private PasswordEncoder encoder;

    @BeforeEach
    void configurarMocks() {
        lenient().when(encoder.encode(any())).thenReturn("hash");
        lenient().when(usuarios.save(any(Usuario.class))).thenAnswer(chamada -> chamada.getArgument(0));
    }

    private UsuarioService servico(boolean permitirEscolhaDePerfil) {
        return new UsuarioService(usuarios, encoder,
                Dados.propriedades(permitirEscolhaDePerfil, false, "", "gemini"), new UsuarioMapper());
    }

    private static RegistroRequest cadastro(Perfil perfil) {
        return new RegistroRequest("Maria", "  Maria@Teste.com ", "segredo1", perfil, "Cargo", null, null);
    }

    @Test
    void respeitaOPerfilPedidoQuandoAEscolhaEstaHabilitada() {
        UsuarioResponse criado = servico(true).registrar(cadastro(Perfil.LIDERANCA));

        assertThat(criado.perfil()).isEqualTo(Perfil.LIDERANCA);
        assertThat(criado.email()).isEqualTo("maria@teste.com");
    }

    @Test
    void todoCadastroPublicoViraOperadorQuandoAEscolhaEstaDesabilitada() {
        assertThat(servico(false).registrar(cadastro(Perfil.LIDERANCA)).perfil()).isEqualTo(Perfil.OPERADOR);
        assertThat(servico(false).registrar(cadastro(Perfil.GESTOR)).perfil()).isEqualTo(Perfil.OPERADOR);
    }

    @Test
    void semPerfilPedidoOCadastroViraOperador() {
        assertThat(servico(true).registrar(cadastro(null)).perfil()).isEqualTo(Perfil.OPERADOR);
    }

    @Test
    void guardaSomenteOHashDaSenha() {
        servico(true).registrar(cadastro(Perfil.OPERADOR));

        verify(usuarios).save(org.mockito.ArgumentMatchers.argThat(u -> "hash".equals(u.getSenhaHash())));
    }

    @Test
    void naoCadastraEmailJaExistente() {
        when(usuarios.existsByEmail("maria@teste.com")).thenReturn(true);

        assertThatThrownBy(() -> servico(true).registrar(cadastro(Perfil.OPERADOR)))
                .isInstanceOf(ConflitoException.class);
        verify(usuarios, never()).save(any(Usuario.class));
    }

    @Test
    void ninguemAlteraOProprioPerfil() {
        var eu = new UsuarioLogado("u-1", "Eu", "eu@teste.com", Perfil.LIDERANCA);

        assertThatThrownBy(() -> servico(true).alterarPerfil("u-1", Perfil.OPERADOR, eu))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void liderancaAlteraOPerfilDeOutroUsuario() {
        Usuario alvo = Dados.comId(new Usuario(new DadosUsuario("Alvo", "alvo@teste.com", "h", Perfil.OPERADOR,
                null, null, null)), "u-2");
        when(usuarios.findById("u-2")).thenReturn(Optional.of(alvo));
        var lideranca = new UsuarioLogado("u-1", "Líder", "lider@teste.com", Perfil.LIDERANCA);

        UsuarioResponse resposta = servico(true).alterarPerfil("u-2", Perfil.GESTOR, lideranca);

        assertThat(resposta.perfil()).isEqualTo(Perfil.GESTOR);
    }
}
