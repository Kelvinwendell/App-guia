package br.com.fiap.aguiainova.domain.model

data class Usuario(
    val id: String,
    val nome: String,
    val email: String,
    val perfil: Perfil,
    val cargo: String? = null,
    val departamento: String? = null,
    val unidade: String? = null,
    val ativo: Boolean = true
)

/** Resposta do login: o token JWT e os dados do usuario, incluindo o perfil que define o que o app exibe. */
data class Autenticacao(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Long,
    val usuario: Usuario
)
