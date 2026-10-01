package com.mision.app.core.time

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Spanish locale shared by every user-facing formatted label. */
val SpanishLocale: Locale = Locale.forLanguageTag("es-ES")

/** Spanish date/time helpers used by the UI. */
object DateFormats {

    private val locale = SpanishLocale

    private val longFormatter = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", locale)
    private val fullFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", locale)
    private val shortFormatter = DateTimeFormatter.ofPattern("d MMM", locale)

    /** "martes, 30 de septiembre" */
    fun longDate(date: LocalDate): String {
        val text = longFormatter.format(date)
        return text.replaceFirstChar { it.uppercase(locale) }
    }

    /** "30 de septiembre de 2026" */
    fun fullDate(date: LocalDate): String = fullFormatter.format(date)

    /** "30 sep" */
    fun shortDate(date: LocalDate): String = shortFormatter.format(date)

    /** Initials used by the weekly chart: L M X J V S D */
    fun dayInitial(date: LocalDate): String {
        val index = (date.dayOfWeek.value + 6) % 7 // Monday == 0
        return charArrayOf('L', 'M', 'X', 'J', 'V', 'S', 'D')[index].toString()
    }

    /** Spanish weekday name, capitalized ("Martes"). */
    fun weekdayName(date: LocalDate): String {
        val text = date.dayOfWeek.getDisplayName(
            java.time.format.TextStyle.FULL,
            locale,
        )
        return text.replaceFirstChar { it.uppercase(locale) }
    }

    fun greeting(hour: Int): String = when (hour) {
        in 0..12 -> "Buenos días"
        in 13..19 -> "Buenas tardes"
        else -> "Buenas noches"
    }

    /** "20:05" style label for reminder pickers. */
    fun hourMinute(hour: Int, minute: Int): String = "%02d:%02d".format(hour, minute)
}
