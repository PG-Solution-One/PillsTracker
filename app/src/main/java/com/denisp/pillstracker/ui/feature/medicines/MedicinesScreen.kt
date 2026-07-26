package com.denisp.pillstracker.ui.feature.medicines

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.data.TrackerRepository
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.model.MedicineState
import com.denisp.pillstracker.model.ScheduleKind
import com.denisp.pillstracker.model.displayAmount
import com.denisp.pillstracker.ui.asTime
import com.denisp.pillstracker.ui.components.MedicineAppearance
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MedicinesScreen(
    medicines: List<Medicine>,
    repository: TrackerRepository,
    onEdit: (Medicine) -> Unit,
    onChanged: () -> Unit,
) {
    var selectedState by remember { mutableStateOf(MedicineState.ACTIVE) }
    var refillMedicine by remember { mutableStateOf<Medicine?>(null) }
    var selectedMedicine by remember { mutableStateOf<Medicine?>(null) }
    val filtered = medicines.filter { it.state == selectedState }

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
                    "Лекарства",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MedicineState.entries.forEach { state ->
                        FilterChip(
                            selected = selectedState == state,
                            onClick = { selectedState = state },
                            label = {
                                Text(
                                    when (state) {
                                        MedicineState.ACTIVE -> "Активные"
                                        MedicineState.PAUSED -> "Пауза"
                                        MedicineState.ARCHIVED -> "Архив"
                                    },
                                )
                            },
                        )
                    }
                }
            }
            if (filtered.isEmpty()) {
                item {
                    Card(shape = RoundedCornerShape(20.dp)) {
                        Column(Modifier.padding(24.dp)) {
                            Text("Здесь пока пусто", fontWeight = FontWeight.SemiBold)
                            Text(
                                if (selectedState == MedicineState.ACTIVE) {
                                    "Нажмите «+», чтобы добавить первое лекарство."
                                } else {
                                    "Лекарства появятся здесь после изменения их статуса."
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else {
                items(filtered, key = { it.id }) { medicine ->
                    MedicineCard(
                        medicine = medicine,
                        onOpen = { selectedMedicine = medicine },
                    )
                }
            }
        }
    }

    refillMedicine?.let { medicine ->
        RefillDialog(
            medicine = medicine,
            onDismiss = { refillMedicine = null },
            onSave = {
                repository.refill(medicine.id, it)
                refillMedicine = null
            },
        )
    }

    selectedMedicine?.let { medicine ->
        MedicineActionsSheet(
            medicine = medicine,
            onDismiss = { selectedMedicine = null },
            onEdit = {
                selectedMedicine = null
                onEdit(medicine)
            },
            onRefill = {
                selectedMedicine = null
                refillMedicine = medicine
            },
            onStateChange = { state ->
                repository.setMedicineState(medicine.id, state)
                onChanged()
                selectedMedicine = null
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MedicineCard(
    medicine: Medicine,
    onOpen: () -> Unit,
) {
    Card(
        modifier = Modifier.combinedClickable(
            onClick = onOpen,
            onLongClick = onOpen,
        ),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MedicineAppearance(medicine = medicine, size = 30.dp)
                Column(
                    Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                ) {
                    Text(
                        medicine.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        "${medicine.form.title} · ${medicine.dosage}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                scheduleSummary(medicine),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "Осталось ${medicine.remaining.displayAmount()} из ${medicine.packageSize.displayAmount()} шт.",
                color = if (medicine.remaining <= medicine.tabletsPerIntake * 3) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            Text(
                "Нажмите или удерживайте для действий",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MedicineActionsSheet(
    medicine: Medicine,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onRefill: () -> Unit,
    onStateChange: (MedicineState) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MedicineAppearance(medicine = medicine, size = 42.dp)
                Column(Modifier.padding(start = 16.dp)) {
                    Text(
                        medicine.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "${medicine.form.title} · ${medicine.dosage}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(scheduleSummary(medicine))
            Text(
                "Осталось ${medicine.remaining.displayAmount()} из ${medicine.packageSize.displayAmount()} шт.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (medicine.note.isNotBlank()) {
                Text(
                    medicine.note,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(onClick = onEdit, modifier = Modifier.fillMaxWidth()) {
                Text("Редактировать")
            }
            OutlinedButton(onClick = onRefill, modifier = Modifier.fillMaxWidth()) {
                Text("Пополнить остаток")
            }
            when (medicine.state) {
                MedicineState.ACTIVE -> {
                    OutlinedButton(
                        onClick = { onStateChange(MedicineState.PAUSED) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Приостановить")
                    }
                    TextButton(
                        onClick = { onStateChange(MedicineState.ARCHIVED) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Завершить курс")
                    }
                }
                MedicineState.PAUSED -> {
                    OutlinedButton(
                        onClick = { onStateChange(MedicineState.ACTIVE) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Возобновить")
                    }
                    TextButton(
                        onClick = { onStateChange(MedicineState.ARCHIVED) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Перенести в архив")
                    }
                }
                MedicineState.ARCHIVED -> {
                    OutlinedButton(
                        onClick = { onStateChange(MedicineState.ACTIVE) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Возобновить курс")
                    }
                }
            }
        }
    }
}

@Composable
private fun RefillDialog(
    medicine: Medicine,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit,
) {
    var amount by remember(medicine.id) { mutableStateOf(medicine.packageSize.displayAmount()) }
    val parsed = amount.replace(',', '.').toDoubleOrNull()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Пополнить ${medicine.name}") },
        text = {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Таблеток в наличии") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
            )
        },
        confirmButton = {
            Button(
                onClick = { parsed?.let(onSave) },
                enabled = parsed != null && parsed >= 0,
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

private fun scheduleSummary(medicine: Medicine): String {
    if (medicine.scheduleKind == ScheduleKind.AS_NEEDED) return "По необходимости"
    val times = medicine.times.joinToString(", ") { schedule ->
        LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .plusMinutes(schedule.minuteOfDay.toLong())
            .toInstant()
            .toEpochMilli()
            .asTime()
    }
    return "${medicine.scheduleKind.title} · $times"
}
