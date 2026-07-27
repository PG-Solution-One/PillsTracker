package com.denisp.pillstracker.ui.feature.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.data.TrackerRepository
import com.denisp.pillstracker.model.IntakeStatus
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.model.MedicineState
import com.denisp.pillstracker.model.ScheduleKind
import com.denisp.pillstracker.model.ScheduledDose
import com.denisp.pillstracker.model.TrackerSnapshot
import com.denisp.pillstracker.model.displayAmount
import com.denisp.pillstracker.notifications.NotificationScheduler
import com.denisp.pillstracker.ui.FullDateFormatter
import com.denisp.pillstracker.ui.RussianLocale
import com.denisp.pillstracker.ui.components.MedicineAppearance
import com.denisp.pillstracker.ui.components.SwipeableIntakeCard
import com.denisp.pillstracker.ui.components.updateIntakeStatus
import com.denisp.pillstracker.ui.theme.AppEmptyState
import com.denisp.pillstracker.ui.theme.AppSectionHeader
import com.denisp.pillstracker.ui.theme.AppSurfaceCard
import java.time.LocalDate
import java.time.format.TextStyle

@Composable
internal fun HistoryIntakesContent(
    snapshot: TrackerSnapshot,
    repository: TrackerRepository,
    scheduler: NotificationScheduler,
    selectedDate: LocalDate,
    onSelectedDateChanged: (LocalDate) -> Unit,
) {
    val today = LocalDate.now()
    val weekStart = selectedDate.minusDays((selectedDate.dayOfWeek.value - 1).toLong())
    val doses = repository.dosesForDateIncludingManual(
        date = selectedDate,
        activeOnly = !selectedDate.isBefore(today),
    )
    val canEditDate = !selectedDate.isAfter(today)
    val asNeeded = snapshot.medicines.filter {
        selectedDate == today &&
            it.state == MedicineState.ACTIVE &&
            it.scheduleKind == ScheduleKind.AS_NEEDED
    }
    val markDose: (ScheduledDose, IntakeStatus) -> Unit = { dose, status ->
        updateIntakeStatus(repository, scheduler, dose, status)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp,
            top = 14.dp,
            end = 20.dp,
            bottom = 40.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            AppSurfaceCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = { onSelectedDateChanged(selectedDate.minusWeeks(1)) },
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Предыдущая неделя",
                        )
                    }
                    Text(
                        text = selectedDate
                            .format(FullDateFormatter)
                            .replaceFirstChar { it.uppercase() },
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                    IconButton(
                        onClick = { onSelectedDateChanged(selectedDate.plusWeeks(1)) },
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = "Следующая неделя",
                        )
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth()) {
                (0L..6L).forEach { offset ->
                    val date = weekStart.plusDays(offset)
                    val dayDoses = repository.dosesForDateIncludingManual(
                        date = date,
                        activeOnly = !date.isBefore(today),
                    )
                    IntakeDayCell(
                        modifier = Modifier.weight(1f),
                        date = date,
                        selected = date == selectedDate,
                        hasTaken = dayDoses.any { it.status == IntakeStatus.TAKEN },
                        hasSkipped = dayDoses.any { it.status == IntakeStatus.SKIPPED },
                        onClick = { onSelectedDateChanged(date) },
                    )
                }
            }
        }
        item {
            AppSectionHeader(
                title = "Приёмы",
                supportingText = if (canEditDate) {
                    "Свайп вправо — принято, влево — пропущено"
                } else {
                    "Будущие даты доступны только для просмотра"
                },
            )
        }
        if (doses.isEmpty()) {
            item {
                AppEmptyState(
                    title = "На этот день приёмов нет",
                    supportingText = "Выберите другой день или добавьте расписание лекарству.",
                )
            }
        } else {
            items(doses, key = { "${it.medicine.id}-${it.scheduledAt}" }) { dose ->
                SwipeableIntakeCard(
                    dose = dose,
                    canEdit = canEditDate,
                    onStatus = { markDose(dose, it) },
                )
            }
        }
        if (asNeeded.isNotEmpty()) {
            item { AppSectionHeader("По необходимости") }
            items(asNeeded, key = { "as-needed-${it.id}" }) { medicine ->
                AsNeededIntakeCard(
                    medicine = medicine,
                    onTaken = {
                        val now = System.currentTimeMillis() / 60_000L * 60_000L
                        repository.markIntake(medicine.id, now, IntakeStatus.TAKEN)
                        scheduler.showLowStockNotifications(
                            repository.snapshot.value.medicines.filter {
                                it.id == medicine.id
                            },
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun IntakeDayCell(
    modifier: Modifier,
    date: LocalDate,
    selected: Boolean,
    hasTaken: Boolean,
    hasSkipped: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    androidx.compose.ui.graphics.Color.Transparent
                },
                RoundedCornerShape(16.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = date.dayOfWeek
                .getDisplayName(TextStyle.SHORT, RussianLocale)
                .take(2),
            style = MaterialTheme.typography.labelSmall,
        )
        Text(date.dayOfMonth.toString(), fontWeight = FontWeight.SemiBold)
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (hasTaken) {
                Box(
                    Modifier
                        .size(5.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                )
            }
            if (hasSkipped) {
                Box(
                    Modifier
                        .size(5.dp)
                        .background(MaterialTheme.colorScheme.error, CircleShape),
                )
            }
            if (!hasTaken && !hasSkipped) Spacer(Modifier.size(5.dp))
        }
    }
}

@Composable
private fun AsNeededIntakeCard(
    medicine: Medicine,
    onTaken: () -> Unit,
) {
    val canTake = medicine.remaining + 0.000_001 >= medicine.tabletsPerIntake
    AppSurfaceCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MedicineAppearance(medicine = medicine, size = 38.dp)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                Text(medicine.name, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "${medicine.dosage} · осталось " +
                        "${medicine.remaining.displayAmount()} шт.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(onClick = onTaken, enabled = canTake) {
                Text("Принял")
            }
        }
    }
}
