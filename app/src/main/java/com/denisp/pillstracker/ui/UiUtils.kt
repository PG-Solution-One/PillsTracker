package com.denisp.pillstracker.ui

import androidx.compose.ui.graphics.Color
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

val RussianLocale: Locale = Locale.forLanguageTag("ru")
val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", RussianLocale)
val DateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM", RussianLocale)
val FullDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM, EEEE", RussianLocale)

fun Long.asTime(): String =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).format(TimeFormatter)

fun LocalDate.asDate(): String = format(DateFormatter)

fun Long.toComposeColor(): Color = Color(toInt())

val MedicinePalette = listOf(
    0xFFF4F2EC,
    0xFFE8DFC9,
    0xFFE2CF87,
    0xFFE1AE85,
    0xFFD5A1A5,
    0xFFB9504E,
    0xFF7D3B3D,
    0xFF9DB79A,
    0xFF54775B,
    0xFF8A6B57,
    0xFFA5AAA7,
    0xFF8FAFC0,
)
