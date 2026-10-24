package br.com.fiap.aguiainova.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta unica do app. Dark-only por decisao de produto (ver docs/redesign/PRD.md, "Nao objetivos":
 * nao criar tema claro) — valores definidos em docs/redesign/specs/001-fundacoes-visuais.md.
 */
object AguiaCores {
    val Fundo = Color(0xFF0B1220)
    val Superficie = Color(0xFF131F32)
    val SuperficieElevada = Color(0xFF1A2B43)
    val Borda = Color(0xFF2A3C55)
    val Primaria = Color(0xFF53C7F5)
    val Sucesso = Color(0xFF55D9B1)
    val Alerta = Color(0xFFFFCF70)
    val Erro = Color(0xFFFF8892)
    val TextoSecundario = Color(0xFFA7B7CC)
}
