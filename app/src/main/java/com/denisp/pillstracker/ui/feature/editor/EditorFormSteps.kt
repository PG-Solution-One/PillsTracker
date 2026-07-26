package com.denisp.pillstracker.ui.feature.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.model.DosageUnit
import com.denisp.pillstracker.model.MedicineForm
import com.denisp.pillstracker.model.PillShape
import com.denisp.pillstracker.ui.MedicinePalette
import com.denisp.pillstracker.ui.components.MedicineAppearance
import com.denisp.pillstracker.ui.toComposeColor

@Composable
internal fun BasicMedicineStep(
    name: String,
    onNameChanged: (String) -> Unit,
    form: MedicineForm,
    onFormChanged: (MedicineForm) -> Unit,
    pillShape: PillShape,
    onPillShapeChanged: (PillShape) -> Unit,
    colorArgb: Long,
    onColorChanged: (Long) -> Unit,
    secondaryColorArgb: Long?,
    onSecondaryColorChanged: (Long?) -> Unit,
    showError: Boolean,
) {
    OutlinedTextField(
        value = name,
        onValueChange = onNameChanged,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Название лекарства") },
        placeholder = { Text("Например, Витамин D") },
        singleLine = true,
        isError = showError && name.isBlank(),
        supportingText = if (showError && name.isBlank()) {
            { Text("Введите название") }
        } else {
            null
        },
    )
    SelectionField("Форма", form, MedicineForm.entries, onFormChanged, MedicineForm::title)
    SelectionField(
        "Форма таблетки",
        pillShape,
        PillShape.entries,
        onPillShapeChanged,
        PillShape::title,
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text("Два цвета", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "Для капсул и двухцветных таблеток",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = secondaryColorArgb != null,
            onCheckedChange = {
                onSecondaryColorChanged(if (it) MedicinePalette[1] else null)
            },
        )
    }
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        MedicineAppearance(
            shape = pillShape,
            primaryColorArgb = colorArgb,
            secondaryColorArgb = secondaryColorArgb,
            size = 54.dp,
        )
    }
    MedicineColorPicker(
        title = if (secondaryColorArgb == null) "Цвет лекарства" else "Первая половина",
        selectedColor = colorArgb,
        onColorChanged = onColorChanged,
    )
    secondaryColorArgb?.let { secondaryColor ->
        MedicineColorPicker(
            title = "Вторая половина",
            selectedColor = secondaryColor,
            onColorChanged = { onSecondaryColorChanged(it) },
        )
    }
}

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
internal fun DecimalField(
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

@Composable
internal fun <T> SelectionField(
    label: String,
    selected: T,
    options: List<T>,
    onSelected: (T) -> Unit,
    title: (T) -> String,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = modifier) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(label, style = MaterialTheme.typography.labelSmall)
                    Text(title(selected), style = MaterialTheme.typography.bodyLarge)
                }
                Text("⌄")
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(title(option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun MedicineColorPicker(
    title: String,
    selectedColor: Long,
    onColorChanged: (Long) -> Unit,
) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MedicinePalette.forEach { color ->
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        if (color == selectedColor) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                        CircleShape,
                    )
                    .padding(3.dp)
                    .background(color.toComposeColor(), CircleShape)
                    .clickable { onColorChanged(color) },
            )
        }
    }
}
