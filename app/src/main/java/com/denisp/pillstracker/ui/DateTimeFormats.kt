package com.denisp.pillstracker.ui

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

val RussianLocale: Locale = Locale.forLanguageTag("ru")
val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", RussianLocale)
val DateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM", RussianLocale)
val DateWithYearFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMMM yyyy", RussianLocale)
val FullDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM, EEEE", RussianLocale)

fun Long.asTime(): String =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).format(TimeFormatter)

fun LocalDate.asDate(): String = format(DateFormatter)
