package com.denisp.pillstracker.ui.feature.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.ui.theme.AppSurfaceCard
import java.time.LocalDate

private val WeekdayLabels = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

@Composable
internal fun FourWeekHistoryCalendar(
    days: List<DayStatistics>,
    today: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
) {
    AppSurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        elevated = true,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Календарь",
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Row(Modifier.fillMaxWidth()) {
                WeekdayLabels.forEach { label ->
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            days.chunked(7).forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    week.forEach { day ->
                        CalendarDay(
                            modifier = Modifier.weight(1f),
                            day = day,
                            today = today,
                            onClick = { onDateSelected(day.date) },
                        )
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
            CalendarLegend()
        }
    }
}

@Composable
private fun CalendarDay(
    modifier: Modifier,
    day: DayStatistics,
    today: LocalDate,
    onClick: () -> Unit,
) {
    val isFuture = day.date.isAfter(today)
    val statusColor = when {
        day.skipped > 0 && day.taken > 0 -> HistoryPartialStatusColor
        day.skipped > 0 -> MaterialTheme.colorScheme.error
        day.taken > 0 && day.pending > 0 -> HistoryPartialStatusColor
        day.taken > 0 -> MaterialTheme.colorScheme.primary
        day.pending > 0 -> HistoryPartialStatusColor
        else -> Color.Transparent
    }
    val isToday = day.date == today
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(
                color = if (isToday) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    Color.Transparent
                },
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(enabled = !isFuture, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(
                    alpha = if (isFuture) 0.35f else 1f,
                ),
            )
            Box(
                Modifier
                    .size(5.dp)
                    .background(statusColor, CircleShape),
            )
        }
    }
}

@Composable
private fun CalendarLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendItem(MaterialTheme.colorScheme.primary, "Принято")
        LegendItem(HistoryPartialStatusColor, "Частично")
        LegendItem(MaterialTheme.colorScheme.error, "Пропущено")
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
) {
    Row(
        modifier = Modifier.padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            Modifier
                .size(6.dp)
                .background(color, CircleShape),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
