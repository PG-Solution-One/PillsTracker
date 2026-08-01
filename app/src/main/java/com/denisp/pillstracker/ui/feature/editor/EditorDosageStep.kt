package com.denisp.pillstracker.ui.feature.editor

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import com.denisp.pillstracker.model.DosageUnit
import com.denisp.pillstracker.ui.theme.AppTextField

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
    trackStock: Boolean,
    onTrackStockChanged: (Boolean) -> Unit,
    showError: Boolean,
) {
    val tabletsFocusRequester = remember { FocusRequester() }
    val packageFocusRequester = remember { FocusRequester() }
    val remainingFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val finishInput = {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }

    EditorStepContent {
        EditorSectionCard(
            title = "Дозировка",
            supportingText = "Укажите количество лекарства за один приём",
        ) {
            DecimalField(
                value = dosageAmount,
                onValueChanged = onDosageAmountChanged,
                label = "Дозировка",
                placeholder = "Например, 60",
                showError = showError,
                allowZero = false,
                imeAction = ImeAction.Next,
                keyboardActions = KeyboardActions(
                    onNext = { tabletsFocusRequester.requestFocus() },
                ),
            )
            SelectionField(
                label = "Единица измерения",
                selected = dosageUnit,
                options = DosageUnit.entries,
                onSelected = onDosageUnitChanged,
                title = DosageUnit::title,
                columns = 3,
            )
            DecimalField(
                value = tabletsPerIntake,
                onValueChanged = onTabletsChanged,
                label = "Таблеток за один приём",
                showError = showError,
                allowZero = false,
                modifier = Modifier.focusRequester(tabletsFocusRequester),
                imeAction = if (trackStock) ImeAction.Next else ImeAction.Done,
                keyboardActions = KeyboardActions(
                    onNext = { packageFocusRequester.requestFocus() },
                    onDone = { finishInput() },
                ),
            )
        }

        EditorSectionCard(
            title = "Запас",
            supportingText = "Поможем вовремя заметить, что лекарство заканчивается",
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Следить за запасом",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Switch(
                    checked = trackStock,
                    onCheckedChange = onTrackStockChanged,
                )
            }
            if (trackStock) {
                DecimalField(
                    value = packageSize,
                    onValueChanged = onPackageChanged,
                    label = "Таблеток в полной упаковке",
                    showError = showError,
                    allowZero = false,
                    modifier = Modifier.focusRequester(packageFocusRequester),
                    imeAction = ImeAction.Next,
                    keyboardActions = KeyboardActions(
                        onNext = { remainingFocusRequester.requestFocus() },
                    ),
                )
                DecimalField(
                    value = remaining,
                    onValueChanged = onRemainingChanged,
                    label = "Сейчас осталось",
                    showError = showError,
                    allowZero = true,
                    modifier = Modifier.focusRequester(remainingFocusRequester),
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(
                        onDone = { finishInput() },
                    ),
                )
                Text(
                    "Напоминание о покупке появится, когда останется не больше трёх приёмов.",
                )
            }
        }
    }
}

@Composable
private fun DecimalField(
    value: String,
    onValueChanged: (String) -> Unit,
    label: String,
    showError: Boolean,
    allowZero: Boolean,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    imeAction: ImeAction = ImeAction.Done,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val parsed = value.replace(',', '.').toDoubleOrNull()
    val invalid = parsed == null || if (allowZero) parsed < 0 else parsed <= 0
    AppTextField(
        value = value,
        onValueChange = onValueChanged,
        label = label,
        modifier = modifier,
        placeholder = placeholder,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = imeAction,
        ),
        keyboardActions = keyboardActions,
        isError = showError && invalid,
        supportingText = if (showError && invalid) {
            {
                Text(
                    if (allowZero) {
                        "Введите число не меньше нуля"
                    } else {
                        "Введите число больше нуля"
                    },
                )
            }
        } else {
            null
        },
    )
}
