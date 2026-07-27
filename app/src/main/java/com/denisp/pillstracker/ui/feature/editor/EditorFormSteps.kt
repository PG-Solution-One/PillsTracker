package com.denisp.pillstracker.ui.feature.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.model.DosageUnit
import com.denisp.pillstracker.model.MedicineForm
import com.denisp.pillstracker.model.PillShape
import com.denisp.pillstracker.ui.MedicineBackgroundPalette
import com.denisp.pillstracker.ui.MedicinePalette
import com.denisp.pillstracker.ui.components.MedicineFormSticker
import kotlinx.coroutines.launch
import kotlin.math.abs

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
    backgroundColorArgb: Long,
    onBackgroundColorChanged: (Long) -> Unit,
    showError: Boolean,
) {
    OutlinedTextField(
        value = name,
        onValueChange = onNameChanged,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Название лекарства") },
        placeholder = { Text("Например, Витамин D") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
        ),
        isError = showError && name.isBlank(),
        supportingText = if (showError && name.isBlank()) {
            { Text("Введите название") }
        } else {
            null
        },
    )
    InlineMedicineFormWheel(
        selected = form,
        pillShape = pillShape,
        primaryColorArgb = colorArgb,
        secondaryColorArgb = secondaryColorArgb,
        backgroundColorArgb = backgroundColorArgb,
        onSelected = onFormChanged,
    )
    if (form == MedicineForm.TABLET) {
        SelectionField(
            label = "Форма таблетки",
            selected = pillShape,
            options = PillShape.entries.filter { it != PillShape.CAPSULE },
            onSelected = onPillShapeChanged,
            title = PillShape::title,
        )
    }
    if (form == MedicineForm.TABLET || form == MedicineForm.CAPSULE) {
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
    }
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        MedicineFormSticker(
            form = form,
            shape = pillShape,
            primaryColorArgb = colorArgb,
            secondaryColorArgb = secondaryColorArgb,
            size = 72.dp,
            backgroundColorArgb = backgroundColorArgb,
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
    MedicineColorPicker(
        title = "Фон стикера",
        selectedColor = backgroundColorArgb,
        onColorChanged = onBackgroundColorChanged,
        colors = MedicineBackgroundPalette,
    )
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
private fun InlineMedicineFormWheel(
    selected: MedicineForm,
    pillShape: PillShape,
    primaryColorArgb: Long,
    secondaryColorArgb: Long?,
    backgroundColorArgb: Long,
    onSelected: (MedicineForm) -> Unit,
) {
    val options = MedicineForm.entries
    val initialIndex = options.indexOf(selected).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val scope = rememberCoroutineScope()
    val currentSelected by rememberUpdatedState(selected)
    val currentOnSelected by rememberUpdatedState(onSelected)

    LaunchedEffect(listState, options) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val viewportCenter =
                (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            layoutInfo.visibleItemsInfo.minByOrNull { item ->
                abs(item.offset + item.size / 2 - viewportCenter)
            }?.index
        }.collect { centeredIndex ->
            centeredIndex?.let { index ->
                val centeredForm = options[index]
                if (centeredForm != currentSelected) currentOnSelected(centeredForm)
            }
        }
    }

    Text(
        "Форма лекарства",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
    Text(
        "Прокрутите список — выбранный вид фиксируется в центре",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(216.dp)
                .padding(vertical = 2.dp),
            contentAlignment = Alignment.Center,
        ) {
            LazyColumn(
                state = listState,
                flingBehavior = snapBehavior,
                contentPadding = PaddingValues(vertical = 72.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                itemsIndexed(options) { index, form ->
                    val isCentered = form == selected
                    Surface(
                        onClick = {
                            scope.launch { listState.animateScrollToItem(index) }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .alpha(if (isCentered) 1f else 0.48f),
                        shape = RoundedCornerShape(18.dp),
                        color = if (isCentered) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            Color.Transparent
                        },
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            MedicineFormSticker(
                                form = form,
                                shape = pillShape,
                                primaryColorArgb = if (isCentered) {
                                    primaryColorArgb
                                } else {
                                    MedicinePalette.first()
                                },
                                secondaryColorArgb = secondaryColorArgb.takeIf { isCentered },
                                size = 46.dp,
                                backgroundColorArgb = backgroundColorArgb,
                            )
                            Text(
                                form.title,
                                modifier = Modifier.padding(start = 16.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (isCentered) {
                                    FontWeight.SemiBold
                                } else {
                                    FontWeight.Normal
                                },
                            )
                        }
                    }
                }
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 12.dp)
                    .border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        RoundedCornerShape(20.dp),
                    ),
            )
        }
    }
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
    colors: List<Long> = MedicinePalette,
) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        if (color == selectedColor) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                        CircleShape,
                    )
                    .padding(3.dp)
                    .background(Color(color.toInt()), CircleShape)
                    .clickable { onColorChanged(color) },
            )
        }
    }
}
