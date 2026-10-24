package br.com.fiap.aguiainova.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToString
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import br.com.fiap.aguiainova.AguiaInovaApp
import br.com.fiap.aguiainova.core.di.AppContainer
import br.com.fiap.aguiainova.core.network.Resultado
import br.com.fiap.aguiainova.navigation.NavGraph
import br.com.fiap.aguiainova.ui.theme.AguiaTheme
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Abre o app de verdade (NavGraph completo, repositorios reais) na JVM com o Robolectric, entra na API em execucao,
 * navega clicando como um usuario e salva cada tela em PNG. Serve como teste de interface de ponta a ponta e como
 * "print" do aplicativo. Nao roda no build normal: so quando AGUIA_API_URL aponta para um backend recem-populado com
 * os dados de exemplo (as esperas dependem deles; o teste de contrato acrescenta registros) e AGUIA_CAPTURAS_DIR
 * diz onde salvar as imagens.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
class CapturasDeTelaTest {

    @get:Rule
    val compose: AndroidComposeTestRule<*, ComponentActivity> = createAndroidComposeRule<ComponentActivity>()

    private lateinit var container: AppContainer
    private lateinit var pasta: File

    @Before
    fun preparar() {
        val url = System.getenv("AGUIA_API_URL")
        val destino = System.getenv("AGUIA_CAPTURAS_DIR")
        assumeTrue(
            "Defina AGUIA_API_URL (API recem-populada) e AGUIA_CAPTURAS_DIR para gerar as capturas",
            !url.isNullOrBlank() && !destino.isNullOrBlank()
        )
        container = (ApplicationProvider.getApplicationContext<Application>() as AguiaInovaApp).container
        container.servidor.definir(url!!)
        pasta = File(destino!!).apply { mkdirs() }
    }

    private fun entrarComo(email: String) {
        val resultado = runBlocking { container.auth.entrar(email, "123456") }
        check(resultado is Resultado.Sucesso) { "Login falhou: $resultado" }
    }

    private fun abrirApp() {
        compose.setContent { AguiaTheme { NavGraph(container) } }
    }

    private fun aparece(texto: String) =
        compose.onAllNodesWithText(texto, substring = true).fetchSemanticsNodes().isNotEmpty()

    /**
     * Espera a condicao valer; se nao valer, guarda uma imagem da tela (falha-*.png) e a arvore de semantica para
     * mostrar o que o app exibia naquele momento.
     */
    private fun esperarAte(descricao: String, segundos: Long = 20, condicao: () -> Boolean) {
        try {
            compose.waitUntil(segundos * 1000, condicao)
        } catch (e: ComposeTimeoutException) {
            capturar("falha-" + descricao.filter { it.isLetterOrDigit() })
            val arvore = compose.onRoot().printToString(maxDepth = 30).take(3000)
            throw AssertionError("Nao ocorreu em ${segundos}s: $descricao. Tela:\n$arvore", e)
        }
    }

    private fun esperar(texto: String, segundos: Long = 20) = esperarAte("aparecer \"$texto\"", segundos) { aparece(texto) }

    /** Os icones da barra inferior ficam fora da arvore "mesclada" (o rotulo de texto assume), entao usamos a nao mesclada. */
    private fun irParaAba(rotulo: String) {
        compose.onNode(hasContentDescription(rotulo), useUnmergedTree = true).performClick()
    }

    /**
     * Desenha a janela inteira (a mesma arvore de views que o Android mostraria) num bitmap. Nao usa captureToImage():
     * ele espera um quadro do Choreographer enquanto o Robolectric mantem o looper principal parado neste mesmo thread.
     */
    private fun capturar(nome: String) {
        compose.waitForIdle()
        val janela = compose.activity.window.decorView
        val imagem = Bitmap.createBitmap(janela.width, janela.height, Bitmap.Config.ARGB_8888)
        janela.draw(Canvas(imagem))
        File(pasta, "$nome.png").outputStream().use { imagem.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test
    fun telaDeLogin() {
        abrirApp()
        esperar("Entrar")
        compose.onNodeWithText("Criar conta").assertIsDisplayed()
        capturar("01-login")
    }

    @Test
    fun operador() {
        entrarComo("vinicius@gmail.com")
        abrirApp()
        esperar("Painel do Operador")
        esperar("Aprovadas")
        capturar("02-operador-home")

        irParaAba("Ideias")
        esperar("Máquina de café premium") // a lista e "lazy": esperamos pelo item mais novo, que fica no topo
        capturar("03-operador-ideias")

        compose.onNodeWithText("Aprovadas da empresa").performClick()
        esperar("Sensores de vibração")
        capturar("03b-operador-ideias-aprovadas")

        irParaAba("Estratégia")
        esperar("Segurança e bem-estar do colaborador") // a lista e "lazy": espera pelo item que aparece no topo
        capturar("03c-operador-estrategia")

        compose.onNodeWithText("Segurança e bem-estar do colaborador").performClick()
        esperar("Metas")
        esperar("Tem uma ideia que ajuda nisso?")
        capturar("03d-operador-estrategia-detalhe")
    }

    @Test
    fun operadorCriaIdeia() {
        entrarComo("vinicius@gmail.com")
        abrirApp()
        esperar("Painel do Operador")
        irParaAba("Ideias")
        esperar("Máquina de café premium")

        compose.onNodeWithContentDescription("Nova ideia").performClick()
        esperar("Descreva sua ideia")
        capturar("04-operador-nova-ideia")
    }

    @Test
    fun gestor() {
        entrarComo("anna@gmail.com")
        abrirApp()
        esperar("Retorno parcial")
        esperar("R$")
        capturar("05-gestor-home")

        irParaAba("Ideias")
        esperar("Aprovar")
        capturar("06-gestor-ideias")

        compose.onNodeWithContentDescription("Priorizar com IA").performClick()
        // O ranking chega da API e a lista reordena: as ideias com nota sobem e a rejeitada (sem nota) desce.
        esperarAte("lista ordenada pela nota da IA", 30) { aparece("Checklist digital") && !aparece("Máquina de café premium") }
        capturar("07-gestor-ideias-priorizadas-por-ia")

        irParaAba("Projetos")
        esperar("Painel de indicadores em tempo real")
        capturar("08-gestor-projetos")

        compose.onNodeWithContentDescription("Novo projeto").performClick()
        esperar("Cadastrar projeto")
        capturar("08b-gestor-novo-projeto")
    }

    /** O detalhe do projeto rola (andamento, resultados...); uma janela alta mostra tudo numa imagem. */
    @Test
    @Config(qualifiers = "w411dp-h1700dp-xhdpi")
    fun gestorProjetoDetalhe() {
        entrarComo("anna@gmail.com")
        abrirApp()
        esperar("Painel do Gestor")
        irParaAba("Projetos")
        esperar("Painel de indicadores em tempo real")
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Sensores IoT na linha 3"))
        compose.onNodeWithText("Sensores IoT na linha 3").performClick()
        esperar("Atualizar andamento")
        esperar("Previsão de falhas em produção") // um dos resultados registrados
        capturar("09-gestor-projeto-detalhe")
    }

    /** "Direção" e a tela de estratégias do gestor: saúde de cada estratégia e, na primeira aba, o assistente de IA. */
    @Test
    fun gestorDirecao() {
        entrarComo("anna@gmail.com")
        abrirApp()
        esperar("Painel do Gestor")

        irParaAba("Direção")
        esperar("Próxima ação")
        capturar("07b-gestor-direcao-saude-das-estrategias")

        compose.onNodeWithText("Segurança e bem-estar do colaborador").performClick()
        esperar("Saúde da estratégia")
        capturar("07c-gestor-estrategia-detalhe")
    }

    @Test
    fun assistenteDeIa() {
        entrarComo("anna@gmail.com")
        abrirApp()
        esperar("Assistente de IA")
        compose.onNodeWithText("Assistente de IA").performClick()
        esperar("Sua pergunta")
        esperar("IA ")
        capturar("10-gestor-assistente-ia")

        // a conversa e da tela de Direção: continua ao trocar de aba
        compose.onNodeWithText("Vigentes").performClick()
        esperar("Próxima ação")
        compose.onNodeWithText("Assistente").performClick()
        esperar("Sua pergunta")
    }

    @Test
    fun lideranca() {
        entrarComo("gabriela@gmail.com")
        abrirApp()
        esperar("Produtividade")
        esperar("16")
        capturar("11-lideranca-home")

        irParaAba("Estratégia")
        esperar("Segurança e bem-estar do colaborador") // a lista e "lazy": espera pelo item que aparece no topo
        esperar("em risco")
        capturar("13-lideranca-estrategias")

        compose.onNodeWithText("Segurança e bem-estar do colaborador").performClick()
        esperar("Saúde da estratégia")
        esperar("Ler com IA")
        capturar("13b-lideranca-estrategia-detalhe")
        compose.onNodeWithText("Ler com IA", substring = true).performScrollTo().performClick()
        esperar("Recomendações")
        capturar("13c-lideranca-leitura-da-saude")
    }

    @Test
    fun liderancaCriaEstrategiaComIa() {
        entrarComo("gabriela@gmail.com")
        abrirApp()
        esperar("Painel da Liderança")
        irParaAba("Estratégia")
        esperar("Segurança e bem-estar do colaborador") // a lista e "lazy": espera pelo item que aparece no topo

        compose.onNodeWithContentDescription("Nova estratégia").performClick()
        esperar("Escrever com IA")
        capturar("13d-lideranca-nova-estrategia")

        compose.onNodeWithText("Título").performTextInput("Reduzir desperdício de material")
        compose.onNodeWithText("Descrição").performTextInput("Reduzir o desperdício de material nas linhas de produção.")
        compose.onNodeWithText("Categoria").performTextInput("Sustentabilidade")
        compose.onNodeWithText("Analisar com IA", substring = true).performScrollTo().performClick()
        esperar("O que melhorar")
        capturar("13e-lideranca-analise-da-estrategia")
    }

    @Test
    fun liderancaVeOHistoricoDeEstrategias() {
        entrarComo("gabriela@gmail.com")
        abrirApp()
        esperar("Painel da Liderança")
        irParaAba("Estratégia")
        esperar("Segurança e bem-estar do colaborador") // a lista e "lazy": espera pelo item que aparece no topo

        compose.onNodeWithText("Histórico").performClick()
        esperar("E-commerce 2025") // registro mais recente, no topo da lista
        capturar("14-lideranca-historico-de-estrategias")
    }

    /** O painel tem quatro abas; cada uma e capturada separadamente. */
    @Test
    fun painelDaLideranca() {
        entrarComo("gabriela@gmail.com")
        abrirApp()
        esperar("Painel da Liderança")
        irParaAba("Painel")
        esperar("Produtividade")
        capturar("12-lideranca-painel-resumo")

        compose.onNodeWithText("Estratégias").performClick()
        esperar("Saúde das estratégias")
        esperar("ROI por estratégia")
        capturar("12b-lideranca-painel-estrategias")

        compose.onAllNodesWithText("Projetos").onFirst().performClick() // a primeira e a aba (a segunda, a barra inferior)
        esperar("Retorno por mês")
        capturar("12c-lideranca-painel-projetos")

        compose.onNodeWithText("Insights IA").performClick()
        esperar("Gerar insights")
        capturar("12d-lideranca-painel-insights")
    }
}
