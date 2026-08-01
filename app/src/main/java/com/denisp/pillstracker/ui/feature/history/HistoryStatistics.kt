package com.denisp.pillstracker.ui.feature.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.data.TrackerRepository
import com.denisp.pillstracker.model.TrackerSnapshot
import com.denisp.pillstracker.ui.theme.AppSpacing
import java.time.LocalDate

@Composable
fun HistoryStatistics(
    snapshot: TrackerSnapshot,
    repository: TrackerRepository,
    onDateSelected: (LocalDate) -> Unit,
) {
    val today = LocalDate.now()
    val currentWeekStart = today.minusDays((today.dayOfWeek.value - 1).toLong())
    var anchorWeekStart by remember { mutableStateOf(currentWeekStart) }
    val rangeStart = anchorWeekStart.minusWeeks(3)
    val rangeEnd = anchorWeekStart.plusDays(6)
    val days = remember(snapshot, rangeStart, today) {
        loadStatisticsRange(repository, rangeStart, today)
    }
    val previousDays = remember(snapshot, rangeStart, today) {
        loadStatisticsRange(repository, rangeStart.minusDays(28), today)
    }
    val summary = remember(days, previousDays, today) {
        summarizeHistory(days, previousDays, today)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.Lg),
    ) {
        HistoryPeriodSelector(
            start = rangeStart,
            end = rangeEnd,
            canMoveForward = anchorWeekStart < currentWeekStart,
            onPrevious = { anchorWeekStart = anchorWeekStart.minusWeeks(4) },
            onNext = {
                anchorWeekStart = anchorWeekStart.plusWeeks(4).coerceAtMost(currentWeekStart)
            },
        )
        HistorySummaryCard(summary)
        HistoryInsightRow(summary)
        HistoryTrendCard(days)
        FourWeekHistoryCalendar(
            days = days,
            today = today,
            onDateSelected = onDateSelected,
        )
    }
}
