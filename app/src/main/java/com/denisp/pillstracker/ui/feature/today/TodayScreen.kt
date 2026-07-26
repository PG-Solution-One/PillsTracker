package com.denisp.pillstracker.ui.feature.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.denisp.pillstracker.ui.asTime
import com.denisp.pillstracker.ui.components.MedicineAppearance
import java.time.LocalDate

@Composable
fun TodayScreen(
    snapshot: TrackerSnapshot,
    repository: TrackerRepository,
    scheduler: NotificationScheduler,
    openedScheduledAt: Long?,
    onNotificationHandled: () -> Unit,
) {
    val today = LocalDate.now()
    val doses = repository.dosesForDate(today)
    val grouped = doses.groupBy { it.scheduledAt }.toList()
    val lowStock = snapshot.medicines.filter {
        it.state == MedicineState.ACTIVE &&
            it.remaining <= it.tabletsPerIntake * 3
    }
    val asNeeded = snapshot.medicines.filter {
        it.state == MedicineState.ACTIVE && it.scheduleKind == ScheduleKind.AS_NEEDED
    }
    val next = doses.firstOrNull {
        it.status == IntakeStatus.PENDING && it.scheduledAt >= System.currentTimeMillis()
    } ?: doses.firstOrNull { it.status == IntakeStatus.PENDING }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 20.dp,
                top = 20.dp,
                end = 20.dp,
                bottom = 100.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Text(
                    text = "Сегодня",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = today.format(FullDateFormatter).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (next != null) {
                item { NextDoseCard(next) }
            }

            if (lowStock.isNotEmpty()) {
                item { SectionTitle("Заканчивается") }
                items(lowStock, key = { "stock-${it.id}" }) { medicine ->
                    LowStockCard(medicine)
                }
            }

            item { SectionTitle("Расписание") }
            if (grouped.isEmpty()) {
                item {
                    EmptyCard(
                        title = "На сегодня приёмов нет",
                        subtitle = "Добавьте лекарство кнопкой «+» или отдохните от расписания.",
                    )
                }
            } else {
                items(grouped, key = { it.first }) { (scheduledAt, group) ->
                    DoseGroupCard(
                        scheduledAt = scheduledAt,
                        doses = group,
                        onMark = { dose, status ->
                            repository.markIntake(dose.medicine.id, dose.scheduledAt, status)
                            if (status == IntakeStatus.TAKEN) {
                                scheduler.showLowStockNotifications(
                                    repository.snapshot.value.medicines.filter { it.id == dose.medicine.id },
                                )
                            }
                            if (repository.dosesAt(scheduledAt).none { it.status == IntakeStatus.PENDING }) {
                                scheduler.cancelFollowUps(scheduledAt)
                                scheduler.dismissDoseNotification(scheduledAt)
                            }
                        },
                        onTakeAll = {
                            repository.markAll(scheduledAt, IntakeStatus.TAKEN)
                            scheduler.cancelFollowUps(scheduledAt)
                            scheduler.dismissDoseNotification(scheduledAt)
                            scheduler.showLowStockNotifications(repository.dosesAt(scheduledAt).map { it.medicine })
                        },
                    )
                }
            }

            if (asNeeded.isNotEmpty()) {
                item { SectionTitle("По необходимости") }
                items(asNeeded, key = { "as-needed-${it.id}" }) { medicine ->
                    AsNeededCard(
                        medicine = medicine,
                        onTaken = {
                            val now = System.currentTimeMillis() / 60_000L * 60_000L
                            repository.markIntake(medicine.id, now, IntakeStatus.TAKEN)
                            scheduler.showLowStockNotifications(
                                repository.snapshot.value.medicines.filter { it.id == medicine.id },
                            )
                        },
                    )
                }
            }
        }
    }

    if (openedScheduledAt != null) {
        val openedDoses = repository.dosesAt(openedScheduledAt)
        LaunchedEffect(openedDoses) {
            if (openedDoses.isEmpty()) onNotificationHandled()
        }
        if (openedDoses.isNotEmpty()) {
            DoseDetailsDialog(
                doses = openedDoses,
                onDismiss = onNotificationHandled,
                onMark = { dose, status ->
                    repository.markIntake(dose.medicine.id, dose.scheduledAt, status)
                    if (repository.dosesAt(openedScheduledAt).none { it.status == IntakeStatus.PENDING }) {
                        scheduler.cancelFollowUps(openedScheduledAt)
                        scheduler.dismissDoseNotification(openedScheduledAt)
                        onNotificationHandled()
                    }
                },
                onTakeAll = {
                    repository.markAll(openedScheduledAt, IntakeStatus.TAKEN)
                    scheduler.cancelFollowUps(openedScheduledAt)
                    scheduler.dismissDoseNotification(openedScheduledAt)
                    scheduler.showLowStockNotifications(repository.snapshot.value.medicines)
                    onNotificationHandled()
                },
            )
        }
    }
}

@Composable
private fun NextDoseCard(dose: ScheduledDose) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Ближайший приём · ${dose.scheduledAt.asTime()}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                MedicineDot(dose.medicine)
                Column(Modifier.padding(start = 12.dp)) {
                    Text(
                        dose.medicine.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "${dose.medicine.dosage} · ${dose.medicine.tabletsPerIntake.displayAmount()} шт.",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

@Composable
private fun DoseGroupCard(
    scheduledAt: Long,
    doses: List<ScheduledDose>,
    onMark: (ScheduledDose, IntakeStatus) -> Unit,
    onTakeAll: () -> Unit,
) {
    Card(shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    scheduledAt.asTime(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                if (doses.size > 1 && doses.any { it.status == IntakeStatus.PENDING }) {
                    TextButton(onClick = onTakeAll) { Text("Принять всё") }
                }
            }
            doses.forEach { dose ->
                DoseRow(dose = dose, onMark = { onMark(dose, it) })
            }
        }
    }
}

@Composable
private fun DoseRow(
    dose: ScheduledDose,
    onMark: (IntakeStatus) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MedicineDot(dose.medicine)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                Text(
                    dose.medicine.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    buildString {
                        append(dose.medicine.dosage)
                        append(" · ")
                        append(dose.medicine.tabletsPerIntake.displayAmount())
                        append(" шт.")
                        if (dose.medicine.mealTiming.title != "Неважно") {
                            append(" · ")
                            append(dose.medicine.mealTiming.title)
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            StatusBadge(dose.status)
        }
        if (dose.status == IntakeStatus.PENDING) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onMark(IntakeStatus.TAKEN) }) { Text("Принял") }
                OutlinedButton(onClick = { onMark(IntakeStatus.SKIPPED) }) { Text("Не принял") }
            }
        }
    }
}

@Composable
private fun LowStockCard(medicine: Medicine) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MedicineDot(medicine)
            Column(Modifier.padding(start = 12.dp)) {
                Text("Пора купить ${medicine.name}", fontWeight = FontWeight.SemiBold)
                Text(
                    "Осталось ${medicine.remaining.displayAmount()} шт.",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
    }
}

@Composable
private fun AsNeededCard(medicine: Medicine, onTaken: () -> Unit) {
    Card(shape = RoundedCornerShape(18.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MedicineDot(medicine)
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                Text(medicine.name, fontWeight = FontWeight.SemiBold)
                Text("${medicine.dosage} · осталось ${medicine.remaining.displayAmount()} шт.")
            }
            Button(onClick = onTaken) { Text("Принял") }
        }
    }
}

@Composable
private fun DoseDetailsDialog(
    doses: List<ScheduledDose>,
    onDismiss: () -> Unit,
    onMark: (ScheduledDose, IntakeStatus) -> Unit,
    onTakeAll: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Приём в ${doses.first().scheduledAt.asTime()}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                doses.forEach { dose -> DoseRow(dose) { onMark(dose, it) } }
            }
        },
        confirmButton = {
            if (doses.size > 1 && doses.any { it.status == IntakeStatus.PENDING }) {
                Button(onClick = onTakeAll) { Text("Принял всё") }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } },
    )
}

@Composable
private fun MedicineDot(medicine: Medicine) {
    MedicineAppearance(medicine = medicine, size = 25.dp)
}

@Composable
private fun StatusBadge(status: IntakeStatus) {
    val (text, color) = when (status) {
        IntakeStatus.PENDING -> "Ожидается" to MaterialTheme.colorScheme.surfaceContainerHighest
        IntakeStatus.TAKEN -> "Принято" to Color(0xFFB8E8D3)
        IntakeStatus.SKIPPED -> "Пропущено" to MaterialTheme.colorScheme.errorContainer
    }
    Surface(color = color, shape = CircleShape) {
        Text(text, Modifier.padding(horizontal = 10.dp, vertical = 5.dp), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 6.dp),
    )
}

@Composable
private fun EmptyCard(title: String, subtitle: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(Modifier.padding(24.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
