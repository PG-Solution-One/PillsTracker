package com.denisp.pillstracker.ui.feature.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.data.TrackerRepository
import com.denisp.pillstracker.model.IntakeStatus
import com.denisp.pillstracker.model.ScheduleKind
import com.denisp.pillstracker.model.ScheduledDose
import com.denisp.pillstracker.model.TrackerSnapshot
import com.denisp.pillstracker.ui.FullDateFormatter
import com.denisp.pillstracker.ui.RussianLocale
import com.denisp.pillstracker.ui.asTime
import com.denisp.pillstracker.ui.components.MedicineAppearance
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle

@Composable
fun HistoryScreen(
    snapshot: TrackerSnapshot,
    repository: TrackerRepository,
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val startOfWeek = selectedDate.minusDays((selectedDate.dayOfWeek.value - 1).toLong())
    val scheduledDoses = repository.dosesForDate(selectedDate, activeOnly = false)
    val scheduledKeys = scheduledDoses.map { it.medicine.id to it.scheduledAt }.toSet()
    val manualDoses = snapshot.records
        .filter { record ->
            val date = Instant.ofEpochMilli(record.scheduledAt)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            date == selectedDate && (record.medicineId to record.scheduledAt) !in scheduledKeys
        }
        .mapNotNull { record ->
            snapshot.medicines.firstOrNull { it.id == record.medicineId }?.let { medicine ->
                ScheduledDose(medicine, record.scheduledAt, record.status)
            }
        }
    val doses = (scheduledDoses + manualDoses).sortedBy { it.scheduledAt }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 20.dp,
                top = 20.dp,
                end = 20.dp,
                bottom = 40.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Text(
                    "История",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = { selectedDate = selectedDate.minusWeeks(1) }) { Text("‹ Неделя") }
                    Text(
                        selectedDate.format(FullDateFormatter).replaceFirstChar { it.uppercase() },
                        fontWeight = FontWeight.SemiBold,
                    )
                    TextButton(onClick = { selectedDate = selectedDate.plusWeeks(1) }) { Text("Неделя ›") }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    (0L..6L).forEach { offset ->
                        val date = startOfWeek.plusDays(offset)
                        DayCell(
                            date = date,
                            selected = date == selectedDate,
                            hasTaken = repository.dosesForDate(date, activeOnly = false)
                                .any { it.status == IntakeStatus.TAKEN },
                            hasSkipped = repository.dosesForDate(date, activeOnly = false)
                                .any { it.status == IntakeStatus.SKIPPED },
                            onClick = { selectedDate = date },
                        )
                    }
                }
            }
            item {
                val taken = doses.count { it.status == IntakeStatus.TAKEN }
                val skipped = doses.count { it.status == IntakeStatus.SKIPPED }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryBadge("Принято: $taken", Color(0xFFB8E8D3))
                    SummaryBadge("Пропущено: $skipped", MaterialTheme.colorScheme.errorContainer)
                }
            }
            if (doses.isNotEmpty()) {
                item {
                    Text(
                        "Свайп вправо — принято, влево — пропущено",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (doses.isEmpty()) {
                item {
                    Card(shape = RoundedCornerShape(20.dp)) {
                        Text(
                            "На этот день записей нет",
                            modifier = Modifier.padding(24.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                items(doses, key = { "${it.medicine.id}-${it.scheduledAt}" }) { dose ->
                    HistoryDoseCard(
                        dose = dose,
                        onStatus = {
                            repository.markIntake(dose.medicine.id, dose.scheduledAt, it)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    selected: Boolean,
    hasTaken: Boolean,
    hasSkipped: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    Column(
        modifier = Modifier
            .background(background, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            date.dayOfWeek.getDisplayName(TextStyle.SHORT, RussianLocale).take(2),
            style = MaterialTheme.typography.labelSmall,
        )
        Text(date.dayOfMonth.toString(), fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            if (hasTaken) Box(Modifier.size(5.dp).background(Color(0xFF2A9D6F), CircleShape))
            if (hasSkipped) Box(Modifier.size(5.dp).background(MaterialTheme.colorScheme.error, CircleShape))
        }
    }
}

@Composable
private fun HistoryDoseCard(
    dose: ScheduledDose,
    onStatus: (IntakeStatus) -> Unit,
) {
    val swipeState = rememberSwipeToDismissBoxState(
        positionalThreshold = { distance -> distance * 0.3f },
    )
    LaunchedEffect(swipeState.currentValue) {
        when (swipeState.currentValue) {
            SwipeToDismissBoxValue.StartToEnd -> {
                onStatus(IntakeStatus.TAKEN)
                swipeState.reset()
            }
            SwipeToDismissBoxValue.EndToStart -> {
                onStatus(IntakeStatus.SKIPPED)
                swipeState.reset()
            }
            SwipeToDismissBoxValue.Settled -> Unit
        }
    }

    SwipeToDismissBox(
        state = swipeState,
        backgroundContent = {
            val direction = swipeState.dismissDirection
            val isTaken = direction == SwipeToDismissBoxValue.StartToEnd
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isTaken) Color(0xFFB8E8D3) else MaterialTheme.colorScheme.errorContainer,
                        RoundedCornerShape(18.dp),
                    )
                    .padding(horizontal = 24.dp),
                contentAlignment = if (isTaken) Alignment.CenterStart else Alignment.CenterEnd,
            ) {
                Text(
                    if (isTaken) "Принято ✓" else "Пропущено ✕",
                    fontWeight = FontWeight.Bold,
                    color = if (isTaken) Color(0xFF075E45) else MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
    ) {
        Card(shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MedicineAppearance(medicine = dose.medicine, size = 24.dp)
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                    ) {
                        Text(dose.medicine.name, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${dose.scheduledAt.asTime()} · ${dose.medicine.dosage}" +
                                if (dose.medicine.scheduleKind == ScheduleKind.AS_NEEDED) {
                                    " · по необходимости"
                                } else {
                                    ""
                                },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    HistoryStatus(status = dose.status)
                }
                if (dose.status == IntakeStatus.PENDING) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onStatus(IntakeStatus.TAKEN) }) { Text("Принято") }
                        OutlinedButton(onClick = { onStatus(IntakeStatus.SKIPPED) }) { Text("Пропущено") }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryStatus(status: IntakeStatus) {
    val background = when (status) {
        IntakeStatus.PENDING -> MaterialTheme.colorScheme.surfaceContainerHighest
        IntakeStatus.TAKEN -> Color(0xFFB8E8D3)
        IntakeStatus.SKIPPED -> MaterialTheme.colorScheme.errorContainer
    }
    val foreground = when (status) {
        IntakeStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
        IntakeStatus.TAKEN -> Color(0xFF075E45)
        IntakeStatus.SKIPPED -> MaterialTheme.colorScheme.onErrorContainer
    }
    Surface(color = background, shape = CircleShape) {
        Text(
            text = when (status) {
                IntakeStatus.PENDING -> "Ожидается"
                IntakeStatus.TAKEN -> "Принято"
                IntakeStatus.SKIPPED -> "Пропущено"
            },
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = foreground,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun SummaryBadge(text: String, color: Color) {
    Surface(color = color, shape = CircleShape) {
        Text(text, Modifier.padding(horizontal = 12.dp, vertical = 7.dp), style = MaterialTheme.typography.labelLarge)
    }
}
