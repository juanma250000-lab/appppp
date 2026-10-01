package com.mision.app.core.time

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Spanish date helpers used by the UI. */
object DateFormats {

    private val locale: Locale = Locale.forLanguageTag("es-ES")

    private val longFormatter = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", locale)
    private val shortFormatter = DateTimeFormatter.ofPattern("d MMM", locale)

    /** "Martes 30 de septiembre" */
    fun longDate(date: LocalDate): String =
        longFormatter.format(date).replaceFirstChar { it.uppercase(locale) }

    /** "30 sep" */
    fun shortDate(date: LocalDate): String = shortFormatter.format(date)

    /** Initials used by the weekly chart: L M X J V S D */
    fun dayInitial(date: LocalDate): String = DAY_INITIALS[date.dayOfWeek.value - 1]

    fun greeting(hour: Int): String = when (hour) {
        in 6..12 -> "Buenos días"
        in 13..20 -> "Buenas tardes"
        else -> "Buenas noches"
    }

    /** "08:05" style label for reminder times. */
    fun hourMinute(hour: Int, minute: Int): String = String.format(locale, "%02d:%02d", hour, minute)

    private val DAY_INITIALS = listOf("L", "M", "X", "J", "V", "S", "D")
}
