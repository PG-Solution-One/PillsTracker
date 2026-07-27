package com.denisp.pillstracker.ui.feature.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.ui.RussianLocale
import com.denisp.pillstracker.ui.theme.AppDashboardCard
import com.denisp.pillstracker.ui.theme.AppSurfaceCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private val RangeDateFormatter = DateTimeFormatter.ofPattern("d MMM", RussianLocale)

@Composable
internal fun HistoryPeriodSelector(
    start: LocalDate,
    end: LocalDate,
    canMoveForward: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    AppSurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        elevated = true,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onPrevious) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Предыдущие четыре недели",
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Последние 4 недели",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${start.format(RangeDateFormatter)} — " +
                        end.format(RangeDateFormatter),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onNext, enabled = canMoveForward) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = "Следующие четыре недели",
                )
            }
        }
    }
}

@Composable
internal fun HistorySummaryCard(summary: HistorySummary) {
    AppDashboardCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column {
                    Text(
                        text = "Соблюдение режима",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "По завершённым приёмам",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
                    )
                }
                Text(
                    text = summary.adherence?.let {
                        "${(it * 100).roundToInt()}%"
                    } ?: "—",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Row(Modifier.fillMaxWidth()) {
                SummaryMetric(
                    modifier = Modifier.weight(1f),
                    value = summary.taken,
                    label = "Принято",
                    color = MaterialTheme.colorScheme.primary,
                )
                SummaryMetric(
                    modifier = Modifier.weight(1f),
                    value = summary.skipped,
                    label = "Пропущено",
                    color = MaterialTheme.colorScheme.error,
                )
                SummaryMetric(
                    modifier = Modifier.weight(1f),
                    value = summary.pending,
                    label = "Ожидает",
                    color = HistoryPartialStatusColor,
                )
            }
        }
    }
}

@Composable
private fun SummaryMetric(
    modifier: Modifier,
    value: Int,
    label: String,
    color: Color,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
        )
    }
}

@Composable
internal fun HistoryInsightRow(summary: HistorySummary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        InsightCard(
            modifier = Modifier.weight(1f),
            value = "${summary.streak} дн.",
            label = "Без пропусков",
        )
        InsightCard(
            modifier = Modifier.weight(1f),
            value = summary.adherenceChange?.let {
                if (it > 0) "+$it п.п." else "$it п.п."
            } ?: "—",
            label = "К прошлым 4 нед.",
        )
    }
}

@Composable
private fun InsightCard(
    modifier: Modifier,
    value: String,
    label: String,
) {
    AppSurfaceCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
