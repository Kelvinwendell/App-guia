package br.com.fiap.aguiainova.core.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class InterceptadoresTest {

    private lateinit var servidor: MockWebServer
    private var token: String? = null
    private var sessoesExpiradas = 0

    @Before
    fun preparar() {
        servidor = MockWebServer().apply { start() }
        token = null
        sessoesExpiradas = 0
    }

    @After
    fun encerrar() {
        servidor.shutdown()
    }

    private fun cliente(urlBase: String = servidor.url("/").toString()): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(BaseUrlInterceptor { urlBase })
            .addInterceptor(AuthInterceptor({ token }, { sessoesExpiradas++ }))
            .build()

    private fun chamar(caminho: String, cliente: OkHttpClient = cliente()) {
        cliente.newCall(Request.Builder().url("http://placeholder.invalid$caminho").build()).execute().close()
    }

    @Test
    fun enviaOTokenComoBearerQuandoHaSessao() {
        token = "abc.def.ghi"
        servidor.enqueue(MockResponse().setResponseCode(200))

        chamar("/api/ideias")

        assertEquals("Bearer abc.def.ghi", servidor.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun naoEnviaCabecalhoDeAutorizacaoSemSessao() {
        servidor.enqueue(MockResponse().setResponseCode(200))

        chamar("/api/auth/login")

        assertNull(servidor.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun umQuatrocentosEUmComTokenEncerraASessao() {
        token = "expirado"
        servidor.enqueue(MockResponse().setResponseCode(401))

        chamar("/api/ideias")

        assertEquals(1, sessoesExpiradas)
    }

    @Test
    fun umQuatrocentosEUmNoLoginNaoEncerraASessao() {
        token = "qualquer"
        servidor.enqueue(MockResponse().setResponseCode(401))

        chamar("/api/auth/login")

        assertEquals(0, sessoesExpiradas)
    }

    @Test
    fun umQuatrocentosEUmSemTokenNaoEncerraASessao() {
        servidor.enqueue(MockResponse().setResponseCode(401))

        chamar("/api/ideias")

        assertEquals(0, sessoesExpiradas)
    }

    @Test
    fun oServidorDeDestinoPodeMudarEmTempoDeExecucao() {
        val outroServidor = MockWebServer().apply { start() }
        try {
            var urlAtual = servidor.url("/").toString()
            val cliente = OkHttpClient.Builder().addInterceptor(BaseUrlInterceptor { urlAtual }).build()
            servidor.enqueue(MockResponse().setResponseCode(200))
            outroServidor.enqueue(MockResponse().setResponseCode(200))

            chamar("/api/ideias", cliente)
            urlAtual = outroServidor.url("/").toString()
            chamar("/api/ideias", cliente)

            assertEquals("/api/ideias", servidor.takeRequest().path)
            assertEquals("/api/ideias", outroServidor.takeRequest().path)
        } finally {
            outroServidor.shutdown()
        }
    }

    @Test
    fun preservaOPrefixoDeCaminhoDaUrlBase() {
        servidor.enqueue(MockResponse().setResponseCode(200))

        chamar("/api/ideias?status=APROVADA", cliente(servidor.url("/aguia/").toString()))

        assertEquals("/aguia/api/ideias?status=APROVADA", servidor.takeRequest().path)
    }
}
