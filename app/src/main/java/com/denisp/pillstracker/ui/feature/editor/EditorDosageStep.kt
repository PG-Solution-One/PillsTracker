package com.denisp.pillstracker.ui.feature.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.model.DosageUnit

@Composable
internal fun DosageStep(
    dosageAmount: String,
    onDosageAmountChanged: (String) -> Unit,
    dosageUnit: DosageUnit,
    onDosageUnitChanged: (DosageUnit) -> Unit,
    tabletsPerIntake: String,
    onTabletsChanged: (String) -> Unit,
    packageSize: String,
    onPackageChanged: (String) -> Unit,
    remaining: String,
    onRemainingChanged: (String) -> Unit,
    showError: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = dosageAmount,
            onValueChange = onDosageAmountChanged,
            modifier = Modifier.weight(1f),
            label = { Text("Дозировка") },
            placeholder = { Text("Например, 60") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            isError = showError && (dosageAmount.replace(',', '.').toDoubleOrNull() ?: 0.0) <= 0,
        )
        SelectionField(
            label = "Единица",
            selected = dosageUnit,
            options = DosageUnit.entries,
            onSelected = onDosageUnitChanged,
            title = DosageUnit::title,
            modifier = Modifier.width(128.dp),
        )
    }
    DecimalField(tabletsPerIntake, onTabletsChanged, "Таблеток за один приём", showError)
    DecimalField(packageSize, onPackageChanged, "Таблеток в полной упаковке", showError)
    DecimalField(remaining, onRemainingChanged, "Сейчас осталось", showError)
    Card(shape = RoundedCornerShape(18.dp)) {
        Text(
            "Напоминание о покупке появится, когда останется не больше трёх приёмов.",
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DecimalField(
    value: String,
    onValueChanged: (String) -> Unit,
    label: String,
    showError: Boolean,
) {
    val parsed = value.replace(',', '.').toDoubleOrNull()
    OutlinedTextField(
        value = value,
        onValueChange = onValueChanged,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        isError = showError && (parsed == null || parsed < 0),
    )
}
