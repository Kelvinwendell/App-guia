package br.com.fiap.aguiainova.data.remote.api

import br.com.fiap.aguiainova.domain.model.Autenticacao
import br.com.fiap.aguiainova.domain.model.Credenciais
import br.com.fiap.aguiainova.domain.model.NovoUsuario
import br.com.fiap.aguiainova.domain.model.Usuario
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    @POST("api/auth/login")
    suspend fun entrar(@Body credenciais: Credenciais): Autenticacao

    @POST("api/auth/register")
    suspend fun cadastrar(@Body novo: NovoUsuario): Usuario

    @GET("api/auth/me")
    suspend fun eu(): Usuario
}
