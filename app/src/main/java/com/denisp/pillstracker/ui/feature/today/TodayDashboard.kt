package com.denisp.pillstracker.ui.feature.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.model.ScheduledDose
import com.denisp.pillstracker.model.InterfaceMode
import com.denisp.pillstracker.domain.DoseTimingPolicy
import com.denisp.pillstracker.ui.asTime
import com.denisp.pillstracker.ui.theme.AppDashboardCard
import com.denisp.pillstracker.ui.theme.AppSpacing
import com.denisp.pillstracker.ui.theme.LocalInterfaceMode

@Composable
internal fun TodayOverviewCard(
    activeMedicineCount: Int,
    nextDose: ScheduledDose?,
    takenToday: Int,
    totalToday: Int,
    nowMillis: Long,
) {
    val isOverdue = nextDose?.let { DoseTimingPolicy.isOverdue(it, nowMillis) } == true
    val simplified = LocalInterfaceMode.current == InterfaceMode.SIMPLIFIED
    AppDashboardCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(
                horizontal = AppSpacing.Md,
                vertical = AppSpacing.Xl,
            ),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.Lg),
        ) {
            Text(
                text = "Сегодня",
                modifier = Modifier.padding(horizontal = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            if (simplified) {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.Md)) {
                    SimplifiedDashboardMetric(
                        label = "Активных лекарств",
                        value = activeMedicineCount.toString(),
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    SimplifiedDashboardMetric(
                        label = if (isOverdue) "Просроченный приём" else "Следующий приём",
                        value = nextDose?.scheduledAt?.asTime() ?: "Нет",
                        urgent = isOverdue,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    SimplifiedDashboardMetric(
                        label = "Принято сегодня",
                        value = "$takenToday из $totalToday",
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DashboardMetric(
                        modifier = Modifier.weight(1f),
                        value = activeMedicineCount.toString(),
                        label = "Лекарств",
                    )
                    VerticalDashboardDivider()
                    DashboardMetric(
                        modifier = Modifier.weight(1.2f),
                        value = nextDose?.scheduledAt?.asTime() ?: "—",
                        label = if (isOverdue) "Просрочено" else "Следующий приём",
                        accent = nextDose != null,
                        urgent = isOverdue,
                    )
                    VerticalDashboardDivider()
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.Sm),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = {
                                    if (totalToday == 0) 0f else {
                                        takenToday.toFloat() / totalToday
                                    }
                                },
                                modifier = Modifier.size(54.dp),
                                strokeWidth = 4.dp,
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            )
                            Text(
                                text = "$takenToday/$totalToday",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        Text(
                            text = "Принято",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SimplifiedDashboardMetric(
    label: String,
    value: String,
    urgent: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.Sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.Md),
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = if (urgent) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = if (urgent) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            },
        )
    }
}

@Composable
private fun DashboardMetric(
    modifier: Modifier,
    value: String,
    label: String,
    accent: Boolean = true,
    urgent: Boolean = false,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.Sm),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (urgent) {
                MaterialTheme.colorScheme.error
            } else if (accent) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            maxLines = 1,
        )
        Text(
            text = label,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
            color = if (urgent) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

@Composable
private fun VerticalDashboardDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(68.dp)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}
