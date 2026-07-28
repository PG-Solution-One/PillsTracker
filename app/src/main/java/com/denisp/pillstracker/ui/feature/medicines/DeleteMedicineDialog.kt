package com.denisp.pillstracker.ui.feature.medicines

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.denisp.pillstracker.model.Medicine

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
