package com.denisp.pillstracker.ui.feature.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.data.TrackerRepository
import com.denisp.pillstracker.domain.IntakeRules
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
import com.denisp.pillstracker.ui.components.IntakeStatusControls
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
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = today.format(FullDateFormatter).replaceFirstChar { it.uppercase() },
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
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
                if (
                    doses.size > 1 &&
                    doses.any {
                        it.status == IntakeStatus.PENDING &&
                            IntakeRules.canMarkTaken(
                                it.medicine.remaining,
                                it.medicine.tabletsPerIntake,
                                it.status,
                            )
                    }
                ) {
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
    val canTake = IntakeRules.canMarkTaken(
        remaining = dose.medicine.remaining,
        tabletsPerIntake = dose.medicine.tabletsPerIntake,
        currentStatus = dose.status,
    )
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
            IntakeStatusControls(
                status = dose.status,
                takenEnabled = canTake,
                onStatus = onMark,
            )
        }
        if (!canTake) {
            Text(
                text = "Недостаточно лекарства: осталось ${dose.medicine.remaining.displayAmount()} шт.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
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
    val canTake = IntakeRules.canMarkTaken(
        remaining = medicine.remaining,
        tabletsPerIntake = medicine.tabletsPerIntake,
        currentStatus = IntakeStatus.PENDING,
    )
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
                if (!canTake) {
                    Text(
                        "Лекарство закончилось",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Button(onClick = onTaken, enabled = canTake) { Text("Принял") }
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
            if (
                doses.size > 1 &&
                doses.any {
                    it.status == IntakeStatus.PENDING &&
                        IntakeRules.canMarkTaken(
                            it.medicine.remaining,
                            it.medicine.tabletsPerIntake,
                            it.status,
                        )
                }
            ) {
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
