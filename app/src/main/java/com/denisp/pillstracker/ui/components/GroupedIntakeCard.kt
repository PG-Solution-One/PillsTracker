package com.denisp.pillstracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.model.IntakeStatus
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.model.ScheduledDose
import com.denisp.pillstracker.ui.asTime
import com.denisp.pillstracker.ui.theme.AppSpacing
import com.denisp.pillstracker.ui.theme.AppSurfaceCard

@Composable
fun GroupedIntakeCard(
    doses: List<ScheduledDose>,
    onStatus: (ScheduledDose, IntakeStatus) -> Unit,
    onTakeAll: () -> Unit,
    modifier: Modifier = Modifier,
    isNext: Boolean = false,
    onOpen: ((Medicine) -> Unit)? = null,
    medicineAppearanceSize: Dp = 32.dp,
    prominentTime: Boolean = false,
    takeAllLabelOverride: String? = null,
) {
    if (doses.isEmpty()) return

    val pendingDoses = doses.filter { it.status == IntakeStatus.PENDING }
    val takenCount = doses.count { it.status == IntakeStatus.TAKEN }
    val canTakeAll = pendingDoses.isNotEmpty()
    val takeAllLabel = takeAllLabelOverride ?: when {
        pendingDoses.isEmpty() && takenCount == doses.size -> "Всё принято"
        pendingDoses.isEmpty() -> "Все отмечены"
        pendingDoses.size == doses.size -> "Принять все"
        else -> "Принять оставшиеся"
    }

    AppSurfaceCard(
        modifier = modifier.fillMaxWidth(),
        elevated = true,
    ) {
        Column {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.Lg),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.Md),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.Xs),
                ) {
                    if (prominentTime) {
                        ScheduledTimeBadge(scheduledAt = doses.first().scheduledAt)
                    } else {
                        Text(
                            text = doses.first().scheduledAt.asTime(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Text(
                        text = buildString {
                            append("$takenCount из ${doses.size} принято")
                            if (isNext && pendingDoses.isNotEmpty()) {
                                append(" · следующий приём")
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isNext && pendingDoses.isNotEmpty()) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
                Button(
                    onClick = onTakeAll,
                    enabled = canTakeAll,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                    Text(
                        text = takeAllLabel,
                        modifier = Modifier.padding(start = AppSpacing.Sm),
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            doses.forEachIndexed { index, dose ->
                SwipeableIntakeCard(
                    dose = dose,
                    canEdit = true,
                    onStatus = { status -> onStatus(dose, status) },
                    showScheduledTime = false,
                    isNext = false,
                    onClick = onOpen?.let { open -> { open(dose.medicine) } },
                    embedded = true,
                    medicineAppearanceSize = medicineAppearanceSize,
                )
                if (index != doses.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
            }
        }
    }
}
