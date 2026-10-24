package br.com.fiap.aguiainova.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Cores de marca/status do app — propositalmente as mesmas no tema claro e no escuro, para manter o
 * significado visual (sucesso/alerta/erro) reconhecivel nos dois. Fundo, superficie, borda e texto
 * secundario variam por tema: ficam em [NeutrosEscuro]/[NeutrosClaro], usados em Theme.kt.
 */
object AguiaCores {
    val Primaria = Color(0xFF00A8E8)
    val Sucesso = Color(0xFF00D09C)
    val Alerta = Color(0xFFFFB703)
    val Erro = Color(0xFFE63946)
}

/** Tons neutros do tema escuro (padrao atual do app). */
internal object NeutrosEscuro {
    val Fundo = Color(0xFF0F172A)
    val Superficie = Color(0xFF16213E)
    val SuperficieElevada = Color(0xFF1E2A4A)
    val Borda = Color(0xFF3A4668)
    val TextoSecundario = Color(0xFFB0B8C8)
}

/** Tons neutros do tema claro. */
internal object NeutrosClaro {
    val Fundo = Color(0xFFF7F9FC)
    val Superficie = Color(0xFFFFFFFF)
    val SuperficieElevada = Color(0xFFEEF1F7)
    val Borda = Color(0xFFD9DEE8)
    val TextoSecundario = Color(0xFF5C6477)
}
