package com.denisp.pillstracker.ui.feature.medicines

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.model.MedicineState
import com.denisp.pillstracker.model.displayAmount
import com.denisp.pillstracker.ui.components.MedicineAppearance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MedicineActionsSheet(
    medicine: Medicine,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onRefill: () -> Unit,
    onStateChange: (MedicineState) -> Unit,
    onDelete: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MedicineAppearance(medicine = medicine, size = 52.dp)
                Column(Modifier.padding(start = 16.dp)) {
                    Text(
                        text = medicine.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${medicine.form.title} · ${medicine.dosage}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(medicineScheduleSummary(medicine))
            Text(
                text = "Осталось ${medicine.remaining.displayAmount()} из " +
                    "${medicine.packageSize.displayAmount()} шт.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (medicine.note.isNotBlank()) {
                Text(medicine.note, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = onEdit, modifier = Modifier.fillMaxWidth()) {
                Text("Редактировать")
            }
            OutlinedButton(onClick = onRefill, modifier = Modifier.fillMaxWidth()) {
                Text("Пополнить остаток")
            }
            MedicineStateActions(
                state = medicine.state,
                onStateChange = onStateChange,
            )
            HorizontalDivider(Modifier.padding(top = 4.dp))
            TextButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteForever,
                    contentDescription = null,
                )
                Text(
                    text = "Удалить лекарство",
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun MedicineStateActions(
    state: MedicineState,
    onStateChange: (MedicineState) -> Unit,
) {
    when (state) {
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

@Composable
internal fun DeleteMedicineDialog(
    medicine: Medicine,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Удалить ${medicine.name}?") },
        text = {
            Text(
                "Лекарство, его расписание и вся история приёмов будут удалены навсегда. " +
                    "Это действие нельзя отменить.",
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
            ) {
                Text("Удалить навсегда")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
    )
}
