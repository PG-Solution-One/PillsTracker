package com.denisp.pillstracker.ui.feature.medicines

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.domain.StockRules
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.model.displayAmount
import com.denisp.pillstracker.ui.theme.AppTextField

@Composable
internal fun RefillMedicineDialog(
    medicine: Medicine,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit,
) {
    var amount by remember(medicine.id) {
        mutableStateOf(medicine.packageSize.displayAmount())
    }
    val parsedAmount = amount.replace(',', '.').toDoubleOrNull()
    val remainingAfterRefill = parsedAmount?.let { addedAmount ->
        StockRules.remainingAfterRefill(
            currentRemaining = medicine.remaining,
            addedAmount = addedAmount,
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Пополнить ${medicine.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Сейчас осталось: ${medicine.remaining.displayAmount()} шт.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AppTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = "Добавить в остаток",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                remainingAfterRefill?.let { newRemaining ->
                    Text(
                        text = "После пополнения будет: ${newRemaining.displayAmount()} шт.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { parsedAmount?.let(onSave) },
                enabled = remainingAfterRefill != null,
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
    )
}
