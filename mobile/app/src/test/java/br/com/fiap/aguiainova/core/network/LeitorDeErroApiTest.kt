package br.com.fiap.aguiainova.core.network

import java.io.IOException
import java.net.SocketTimeoutException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class LeitorDeErroApiTest {

    private val leitor = LeitorDeErroApi()

    private fun erroHttp(codigo: Int, corpo: String): HttpException =
        HttpException(Response.error<Any>(codigo, corpo.toResponseBody("application/json".toMediaType())))

    @Test
    fun usaOCampoDetailDaApiComoMensagem() {
        val falha = leitor.traduzir(
            erroHttp(422, """{"type":"about:blank","title":"Unprocessable Content","status":422,"detail":"O prazo não pode ser anterior à data de início."}""")
        )

        assertEquals(TipoErro.VALIDACAO, falha.tipo)
        assertEquals("O prazo não pode ser anterior à data de início.", falha.mensagem)
    }

    @Test
    fun classificaOsStatusHttp() {
        val casos = mapOf(
            400 to TipoErro.VALIDACAO,
            409 to TipoErro.VALIDACAO,
            401 to TipoErro.NAO_AUTENTICADO,
            403 to TipoErro.PROIBIDO,
            404 to TipoErro.NAO_ENCONTRADO,
            503 to TipoErro.INDISPONIVEL,
            500 to TipoErro.SERVIDOR
        )
        casos.forEach { (codigo, tipo) ->
            assertEquals("HTTP $codigo", tipo, leitor.traduzir(erroHttp(codigo, "{}")).tipo)
        }
    }

    @Test
    fun semDetailUsaMensagemPadraoEmPortugues() {
        val falha = leitor.traduzir(erroHttp(403, ""))

        assertEquals("Seu perfil não tem permissão para esta operação.", falha.mensagem)
    }

    @Test
    fun corpoQueNaoEJsonNaoQuebra() {
        val falha = leitor.traduzir(erroHttp(500, "<html>Bad gateway</html>"))

        assertEquals(TipoErro.SERVIDOR, falha.tipo)
        assertTrue(falha.mensagem.isNotBlank())
    }

    @Test
    fun faltaDeConexaoViraMensagemDeConexao() {
        val falha = leitor.traduzir(IOException("sem rede"))

        assertEquals(TipoErro.REDE, falha.tipo)
        assertTrue(falha.mensagem.startsWith("Sem conexão"))
    }

    /** v2.1: timeout tem mensagem propria (hospedagem gratuita hiberna e demora a acordar), distinta de "sem rede". */
    @Test
    fun timeoutViraMensagemDeServidorAcordando() {
        val falha = leitor.traduzir(SocketTimeoutException("tempo esgotado"))

        assertEquals(TipoErro.REDE, falha.tipo)
        assertTrue(falha.mensagem.contains("acordando"))
    }

    @Test
    fun erroInesperadoPreservaAMensagem() {
        val falha = leitor.traduzir(IllegalStateException("bug"))

        assertEquals(TipoErro.DESCONHECIDO, falha.tipo)
        assertEquals("bug", falha.mensagem)
    }
}
