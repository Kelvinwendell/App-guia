package br.com.fiap.aguiainova.data

import br.com.fiap.aguiainova.core.network.ApiClient
import br.com.fiap.aguiainova.core.network.LeitorDeErroApi
import br.com.fiap.aguiainova.core.network.Resultado
import br.com.fiap.aguiainova.core.network.TipoErro
import br.com.fiap.aguiainova.core.session.ArmazenamentoSessaoEmMemoria
import br.com.fiap.aguiainova.core.session.GerenciadorSessao
import br.com.fiap.aguiainova.data.remote.api.AuthApi
import br.com.fiap.aguiainova.data.remote.api.DashboardApi
import br.com.fiap.aguiainova.data.remote.api.EstrategiaApi
import br.com.fiap.aguiainova.data.remote.api.IaApi
import br.com.fiap.aguiainova.data.remote.api.IdeiaApi
import br.com.fiap.aguiainova.data.remote.api.ProjetoApi
import br.com.fiap.aguiainova.data.repository.AuthRepositoryRemoto
import br.com.fiap.aguiainova.data.repository.DashboardRepositoryRemoto
import br.com.fiap.aguiainova.data.repository.EstrategiaRepositoryRemoto
import br.com.fiap.aguiainova.data.repository.IaRepositoryRemoto
import br.com.fiap.aguiainova.data.repository.IdeiaRepositoryRemoto
import br.com.fiap.aguiainova.data.repository.ProjetoRepositoryRemoto
import br.com.fiap.aguiainova.domain.model.AndamentoProjeto
import br.com.fiap.aguiainova.domain.model.AvaliacaoIdeia
import br.com.fiap.aguiainova.domain.model.DadosEstrategia
import br.com.fiap.aguiainova.domain.model.DadosIdeia
import br.com.fiap.aguiainova.domain.model.DadosProjeto
import br.com.fiap.aguiainova.domain.model.DadosResultado
import br.com.fiap.aguiainova.domain.model.EtapaProjeto
import br.com.fiap.aguiainova.domain.model.MensagemChat
import br.com.fiap.aguiainova.domain.model.NovoUsuario
import br.com.fiap.aguiainova.domain.model.PapelMensagem
import br.com.fiap.aguiainova.domain.model.Perfil
import br.com.fiap.aguiainova.domain.model.Prioridade
import br.com.fiap.aguiainova.domain.model.StatusEstrategia
import br.com.fiap.aguiainova.domain.model.StatusIdeia
import br.com.fiap.aguiainova.domain.model.StatusProjeto
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test

/**
 * Teste de contrato: executa os repositorios REAIS do app (Retrofit + Gson + interceptors) contra a API em execucao e
 * confere que cada resposta JSON vira o modelo esperado. So roda quando a variavel de ambiente AGUIA_API_URL aponta para
 * um backend com os dados de exemplo (ex.: AGUIA_API_URL=http://localhost:8080/); sem ela, os testes sao ignorados.
 *
 * Atencao: os testes cadastram usuarios, ideias e estrategias de verdade (a API nao tem exclusao de usuarios e o
 * historico de estrategias e permanente). Rode-os contra um ambiente descartavel (docker compose down -v && up).
 */
class ContratoComApiTest {

    private class Ambiente(url: String) {
        val sessao = GerenciadorSessao(ArmazenamentoSessaoEmMemoria())
        private val leitor = LeitorDeErroApi()
        private val cliente = ApiClient({ url }, sessao::token, sessao::encerrar)
        val auth = AuthRepositoryRemoto(cliente.criar(AuthApi::class.java), sessao, leitor)
        val estrategias = EstrategiaRepositoryRemoto(cliente.criar(EstrategiaApi::class.java), leitor)
        val ideias = IdeiaRepositoryRemoto(cliente.criar(IdeiaApi::class.java), leitor)
        val projetos = ProjetoRepositoryRemoto(cliente.criar(ProjetoApi::class.java), leitor)
        val dashboard = DashboardRepositoryRemoto(cliente.criar(DashboardApi::class.java), leitor)
        val ia = IaRepositoryRemoto(cliente.criar(IaApi::class.java), leitor)
    }

    private lateinit var ambiente: Ambiente

    @Before
    fun preparar() {
        val url = System.getenv("AGUIA_API_URL")
        assumeTrue("Defina AGUIA_API_URL para rodar o teste de contrato", !url.isNullOrBlank())
        ambiente = Ambiente(url!!)
    }

    private fun <T> Resultado<T>.dados(): T = when (this) {
        is Resultado.Sucesso -> dados
        is Resultado.Falha -> error("Falhou: $tipo - $mensagem")
    }

    private suspend fun entrarComo(email: String): Perfil? =
        ambiente.auth.entrar(email, "123456").dados().perfil

    @Test
    fun cadaUsuarioDeTesteEntraComOPerfilDaDocumentacao() = runTest {
        assertEquals(Perfil.OPERADOR, entrarComo("vinicius@gmail.com"))
        assertEquals(Perfil.GESTOR, entrarComo("anna@gmail.com"))
        assertEquals(Perfil.LIDERANCA, entrarComo("gabriela@gmail.com"))
        assertNotNull(ambiente.sessao.token())
    }

    @Test
    fun loginInvalidoTraduzAMensagemDaApi() = runTest {
        val resultado = ambiente.auth.entrar("vinicius@gmail.com", "errada")

        val falha = resultado as Resultado.Falha
        assertEquals(TipoErro.NAO_AUTENTICADO, falha.tipo)
        assertEquals("E-mail ou senha inválidos.", falha.mensagem)
        assertNull(ambiente.sessao.token())
    }

    @Test
    fun cadastroPublicoCriaUsuarioQueConsegueEntrar() = runTest {
        val email = "app-${UUID.randomUUID()}@teste.com"

        val criado = ambiente.auth.cadastrar(NovoUsuario("Teste App", "  $email ", "segredo1", Perfil.OPERADOR)).dados()

        assertEquals(email, criado.email)
        assertEquals(Perfil.OPERADOR, ambiente.auth.entrar(email, "segredo1").dados().perfil)
    }

    @Test
    fun operadorFazOCrudDasPropriasIdeiasEOGestorAvalia() = runTest {
        entrarComo("vinicius@gmail.com")
        val titulo = "Ideia do teste ${UUID.randomUUID()}"
        val criada = ambiente.ideias.criar(DadosIdeia(titulo, "Descrição de teste", "Tecnologia")).dados()
        assertEquals(StatusIdeia.EM_ANALISE, criada.status)
        assertEquals(Prioridade.MEDIA, criada.prioridade)
        assertNull("O operador nao ve a nota da IA", criada.avaliacaoIa)

        val editada = ambiente.ideias.atualizar(criada.id, DadosIdeia("$titulo (editada)", "Nova descrição", "Tecnologia")).dados()
        assertTrue(editada.titulo.endsWith("(editada)"))
        assertTrue(ambiente.ideias.listar().dados().any { it.id == criada.id })

        entrarComo("anna@gmail.com")
        val avaliada = ambiente.ideias.avaliar(
            criada.id, AvaliacaoIdeia(StatusIdeia.APROVADA, Prioridade.ALTA, "Aprovada no teste")
        ).dados()
        assertEquals(StatusIdeia.APROVADA, avaliada.status)
        assertEquals("Aprovada no teste", avaliada.parecer)
        assertEquals("Anna", avaliada.avaliadorNome)

        entrarComo("vinicius@gmail.com")
        val bloqueio = ambiente.ideias.remover(criada.id) as Resultado.Falha
        assertEquals("Ideia ja avaliada nao pode ser excluida", TipoErro.VALIDACAO, bloqueio.tipo)
        assertTrue(ambiente.ideias.aprovadas().dados().any { it.id == criada.id })
    }

    @Test
    fun operadorRemoveIdeiaAindaEmAnalise() = runTest {
        entrarComo("vinicius@gmail.com")
        val criada = ambiente.ideias.criar(DadosIdeia("Descartável ${UUID.randomUUID()}", "Texto", "Geral")).dados()

        ambiente.ideias.remover(criada.id).dados()

        assertTrue(ambiente.ideias.listar().dados().none { it.id == criada.id })
    }

    @Test
    fun gestorLeTodasAsIdeiasEFiltraPorStatus() = runTest {
        entrarComo("anna@gmail.com")

        val todas = ambiente.ideias.listar().dados()
        val emAnalise = ambiente.ideias.listar(status = StatusIdeia.EM_ANALISE).dados()

        assertTrue(todas.isNotEmpty())
        assertTrue(emAnalise.all { it.status == StatusIdeia.EM_ANALISE })
        assertTrue(todas.first().autorNome.isNotBlank())
    }

    @Test
    fun estrategiasTemCamposDeHistoricoEExigemPerfilDeLiderancaParaMudar() = runTest {
        entrarComo("vinicius@gmail.com")
        val lista = ambiente.estrategias.listar(StatusEstrategia.VIGENTE).dados()
        assertTrue(lista.isNotEmpty() && lista.all { it.vigente })
        val negado = ambiente.estrategias.criar(DadosEstrategia("X", "Y", "Z")) as Resultado.Falha
        assertEquals(TipoErro.PROIBIDO, negado.tipo)

        entrarComo("gabriela@gmail.com")
        val titulo = "Estratégia do teste ${UUID.randomUUID()}"
        val criada = ambiente.estrategias.criar(DadosEstrategia(titulo, "Descrição", "Teste", "Campanha T")).dados()
        assertEquals("Campanha T", criada.campanha)
        ambiente.estrategias.atualizar(criada.id, DadosEstrategia(titulo, "Descrição 2", "Teste", "Campanha T", StatusEstrategia.ENCERRADA)).dados()
        ambiente.estrategias.remover(criada.id).dados()

        val historico = ambiente.estrategias.historico(categoria = "teste", campanha = "campanha t").dados()
        assertTrue(historico.count { it.estrategiaId == criada.id } >= 3)
        assertTrue(historico.all { it.data.isNotBlank() && it.categoria.isNotBlank() })
    }

    @Test
    fun gestorGerenciaProjetoComProgressoEResultados() = runTest {
        entrarComo("anna@gmail.com")
        val hoje = LocalDate.now()
        val criado = ambiente.projetos.criar(
            DadosProjeto("Projeto do teste ${UUID.randomUUID()}", "Descrição", 1000.50, hoje.toString(), hoje.plusDays(30).toString())
        ).dados()
        assertEquals(StatusProjeto.PLANEJAMENTO, criado.status)
        assertEquals(EtapaProjeto.INICIACAO, criado.etapa)
        assertEquals(1000.50, criado.investimento, 0.0)

        val emAndamento = ambiente.projetos.atualizarAndamento(
            criado.id, AndamentoProjeto(StatusProjeto.EM_ANDAMENTO, EtapaProjeto.EXECUCAO, 40)
        ).dados()
        assertEquals(40, emAndamento.progresso)

        val comResultado = ambiente.projetos.adicionarResultado(
            criado.id, DadosResultado(descricao = "Ganho", retornoFinanceiro = 1500.25, ganhoProdutividade = 10.0)
        ).dados()
        assertEquals(1500.25, comResultado.retornoFinanceiro, 0.001)
        assertEquals(499.75, comResultado.lucro, 0.001)
        assertNotNull(comResultado.roi)
        assertEquals(1, comResultado.resultados.size)

        val semResultado = ambiente.projetos.removerResultado(criado.id, comResultado.resultados.first().id).dados()
        assertTrue(semResultado.resultados.isEmpty())

        ambiente.projetos.remover(criado.id).dados()
    }

    @Test
    fun operadorNaoAcessaProjetosELiderancaSoConsulta() = runTest {
        entrarComo("vinicius@gmail.com")
        assertEquals(TipoErro.PROIBIDO, (ambiente.projetos.listar() as Resultado.Falha).tipo)

        entrarComo("gabriela@gmail.com")
        val projetos = ambiente.projetos.listar().dados()
        assertTrue(projetos.isNotEmpty())
        assertTrue(projetos.all { it.prazo.isNotBlank() })
        val negado = ambiente.projetos.criar(DadosProjeto("X", "Y", 1.0, prazo = "2999-01-01")) as Resultado.Falha
        assertEquals(TipoErro.PROIBIDO, negado.tipo)
    }

    @Test
    fun dashboardDaLiderancaTrazIndicadoresEGraficos() = runTest {
        entrarComo("gabriela@gmail.com")

        val resumo = ambiente.dashboard.resumo().dados()
        assertTrue(resumo.totalProjetos > 0)
        assertNotNull(resumo.roiPercentual)
        assertTrue(resumo.projetosPorStatus.containsKey("EM_ANDAMENTO"))
        assertTrue(resumo.ideias.total > 0)

        val estrategias = ambiente.dashboard.porEstrategia().dados()
        assertTrue(estrategias.isNotEmpty())
        val filtrado = ambiente.dashboard.resumo(estrategiaId = estrategias.first { it.estrategiaId != null && it.projetos > 0 }.estrategiaId).dados()
        assertTrue(filtrado.totalProjetos <= resumo.totalProjetos)

        assertTrue(ambiente.dashboard.porProjeto().dados().isNotEmpty())
        assertTrue(ambiente.dashboard.evolucao().dados().all { it.mes.length == 7 })

        entrarComo("anna@gmail.com")
        assertEquals(TipoErro.PROIBIDO, (ambiente.dashboard.resumo() as Resultado.Falha).tipo)
    }

    @Test
    fun iaPontuaIdeiasGeraInsightsEInformaOStatus() = runTest {
        entrarComo("anna@gmail.com")
        val status = ambiente.ia.status().dados()
        val ranking = ambiente.ia.avaliarPendentes().dados()
        ranking.ranking.forEach {
            assertTrue(it.avaliacao.score in 0..100)
            assertTrue(it.avaliacao.justificativa.isNotBlank())
        }
        val primeira = ambiente.ideias.listar(status = StatusIdeia.EM_ANALISE).dados().firstOrNull()
        if (primeira != null) {
            assertEquals(primeira.id, ambiente.ia.avaliarIdeia(primeira.id).dados().ideiaId)
            assertNotNull(ambiente.ideias.obter(primeira.id).dados().avaliacaoIa)
        }

        val chat = ambiente.ia.conversar("Quais ideias priorizar?", listOf(MensagemChat(PapelMensagem.USUARIO, "Oi")))
        if (status.disponivel) {
            assertTrue((chat as Resultado.Sucesso).dados.resposta.isNotBlank())
        } else {
            assertEquals(TipoErro.INDISPONIVEL, (chat as Resultado.Falha).tipo)
        }

        entrarComo("gabriela@gmail.com")
        val insights = ambiente.ia.insights().dados()
        assertTrue(insights.resumo.isNotBlank() && insights.insights.isNotEmpty())
        assertNotNull(insights.geradoEm)
    }
}
