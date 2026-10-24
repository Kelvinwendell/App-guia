package br.com.fiap.aguiainova.util

import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.util.Locale

/** Formatacao de numeros e datas no padrao brasileiro, num unico lugar (antes cada tela montava o proprio texto). */
object Formatadores {

    private val ptBr: Locale = Locale.forLanguageTag("pt-BR")
    private val dataBr: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    /**
     * Para o que a pessoa digita: resolucao ESTRITA. Com a padrao (SMART), "31/02/2027" virava 28/02/2027 sem aviso e
     * o projeto era salvo com um prazo que ninguem escolheu. "uuuu" porque o modo estrito exige ano sem era.
     */
    private val dataDigitada: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)
    private val dataHoraBr: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    private val mesAbreviado: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM/yy", ptBr)

    fun moeda(valor: Double): String = NumberFormat.getCurrencyInstance(ptBr).format(valor)

    /** Valor monetario compacto para cartoes: R$ 58 mil, R$ 1,2 mi. */
    fun moedaCompacta(valor: Double): String {
        val absoluto = kotlin.math.abs(valor)
        return when {
            absoluto >= 1_000_000 -> "R$ " + String.format(ptBr, "%.1f", valor / 1_000_000) + " mi"
            absoluto >= 1_000 -> "R$ " + String.format(ptBr, "%.0f", valor / 1_000) + " mil"
            else -> moeda(valor)
        }
    }

    fun percentual(valor: Double?): String = valor?.let { String.format(ptBr, "%.1f%%", it) } ?: "—"

    fun percentualComSinal(valor: Double?): String = valor?.let { String.format(ptBr, "%+.1f%%", it) } ?: "—"

    /** yyyy-MM-dd (ou instante ISO) -> dd/MM/yyyy; devolve o texto original se nao for uma data valida. */
    fun data(iso: String?): String {
        if (iso.isNullOrBlank()) return "—"
        return runCatching { LocalDate.parse(iso.take(TAMANHO_DATA_ISO)).format(dataBr) }.getOrDefault(iso)
    }

    fun dataHora(iso: String?): String {
        if (iso.isNullOrBlank()) return "—"
        return runCatching {
            Instant.parse(iso).atZone(ZoneId.systemDefault()).format(dataHoraBr)
        }.getOrDefault(data(iso))
    }

    /** Periodo de vigencia em texto ("01/01/2026 a 31/12/2026"), ou nulo quando nao foi definido. */
    fun periodo(inicio: String?, fim: String?): String? = when {
        inicio != null && fim != null -> "${data(inicio)} a ${data(fim)}"
        inicio != null -> "desde ${data(inicio)}"
        fim != null -> "até ${data(fim)}"
        else -> null
    }

    /** yyyy-MM -> "mar/26". */
    fun mes(anoMes: String): String =
        runCatching { YearMonth.parse(anoMes).format(mesAbreviado) }.getOrDefault(anoMes)

    /** dd/MM/yyyy digitado pelo usuario -> yyyy-MM-dd, ou nulo se a data nao existir. */
    fun dataIsoDeDigitada(texto: String): String? =
        runCatching { LocalDate.parse(texto.trim(), dataDigitada).toString() }.getOrNull()

    /**
     * Aceita o formato brasileiro (1.234,56) e o internacional (1234.56). Com virgula, o ponto e separador de milhar;
     * sem virgula, o ponto e separador decimal.
     */
    fun numeroDigitado(texto: String): Double? {
        val limpo = texto.trim().removePrefix("R$").trim()
        if (limpo.isEmpty()) return null
        val padrao = if (limpo.contains(',')) limpo.replace(".", "").replace(',', '.') else limpo
        return padrao.toDoubleOrNull()
    }

    private const val TAMANHO_DATA_ISO = 10
}
