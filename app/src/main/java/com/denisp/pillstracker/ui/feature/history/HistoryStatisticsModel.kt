package com.denisp.pillstracker.ui.feature.history

import androidx.compose.ui.graphics.Color
import com.denisp.pillstracker.data.TrackerRepository
import com.denisp.pillstracker.model.IntakeStatus
import java.time.LocalDate
import kotlin.math.roundToInt

internal val HistoryPartialStatusColor = Color(0xFFE7A327)

internal data class DayStatistics(
    val date: LocalDate,
    val taken: Int,
    val skipped: Int,
    val pending: Int,
) {
    val resolved: Int get() = taken + skipped
    val adherence: Float? get() = resolved.takeIf { it > 0 }?.let { taken.toFloat() / it }
}

internal data class HistorySummary(
    val adherence: Float?,
    val taken: Int,
    val skipped: Int,
    val pending: Int,
    val adherenceChange: Int?,
    val streak: Int,
)

internal fun summarizeHistory(
    days: List<DayStatistics>,
    previousDays: List<DayStatistics>,
    today: LocalDate,
): HistorySummary {
    val taken = days.sumOf { it.taken }
    val skipped = days.sumOf { it.skipped }
    val pending = days.sumOf { it.pending }
    val adherence = adherence(taken, skipped)
    val previousAdherence = adherence(
        taken = previousDays.sumOf { it.taken },
        skipped = previousDays.sumOf { it.skipped },
    )
    val adherenceChange = if (adherence == null || previousAdherence == null) {
        null
    } else {
        ((adherence - previousAdherence) * 100).roundToInt()
    }
    val streak = days
        .filter { !it.date.isAfter(today) && it.resolved > 0 }
        .asReversed()
        .takeWhile { it.skipped == 0 }
        .size

    return HistorySummary(
        adherence = adherence,
        taken = taken,
        skipped = skipped,
        pending = pending,
        adherenceChange = adherenceChange,
        streak = streak,
    )
}

internal fun loadStatisticsRange(
    repository: TrackerRepository,
    start: LocalDate,
    today: LocalDate,
    nowMillis: Long = System.currentTimeMillis(),
): List<DayStatistics> = (0L until 28L).map { offset ->
    val date = start.plusDays(offset)
    val doses = if (date.isAfter(today)) {
        emptyList()
    } else {
        repository.dosesForDateIncludingManual(date, activeOnly = false)
    }
    DayStatistics(
        date = date,
        taken = doses.count { it.status == IntakeStatus.TAKEN },
        skipped = doses.count { it.status == IntakeStatus.SKIPPED },
        pending = doses.count {
            it.status == IntakeStatus.PENDING && it.scheduledAt <= nowMillis
        },
    )
}

private fun adherence(taken: Int, skipped: Int): Float? {
    val resolved = taken + skipped
    return if (resolved == 0) null else taken.toFloat() / resolved
}
