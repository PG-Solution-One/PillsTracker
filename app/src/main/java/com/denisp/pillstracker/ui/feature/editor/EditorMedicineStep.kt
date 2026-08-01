package com.denisp.pillstracker.ui.feature.editor

import androidx.compose.animation.core.tween
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.model.MedicineForm
import com.denisp.pillstracker.model.PillShape
import com.denisp.pillstracker.ui.MedicineBackgroundPalette
import com.denisp.pillstracker.ui.MedicinePalette
import com.denisp.pillstracker.ui.theme.AppSpacing
import com.denisp.pillstracker.ui.theme.AppTextField

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
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    EditorStepContent(
        modifier = Modifier.animateContentSize(animationSpec = tween(durationMillis = 180)),
    ) {
        AppTextField(
            value = name,
            onValueChange = onNameChanged,
            label = "Название лекарства",
            placeholder = "Например, Витамин D",
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus(force = true)
                    keyboardController?.hide()
                },
            ),
            isError = showError && name.isBlank(),
            supportingText = if (showError && name.isBlank()) {
                { Text("Введите название") }
            } else {
                null
            },
        )

        EditorSectionCard(
            title = "Форма лекарства",
            supportingText = "Листайте влево или вправо — карусель не заканчивается",
        ) {
            InfiniteMedicineFormPager(
                selected = form,
                pillShape = pillShape,
                primaryColorArgb = colorArgb,
                secondaryColorArgb = secondaryColorArgb,
                backgroundColorArgb = backgroundColorArgb,
                onSelected = onFormChanged,
            )
        }

        if (form == MedicineForm.TABLET) {
            EditorSectionCard(title = "Форма таблетки") {
                SelectionField(
                    label = "Выберите внешний вид",
                    selected = pillShape,
                    options = PillShape.entries.filter { it != PillShape.CAPSULE },
                    onSelected = onPillShapeChanged,
                    title = PillShape::title,
                    columns = 3,
                )
            }
        }

        EditorSectionCard(
            title = "Оформление",
            supportingText = "Цвет лекарства и фон карточки сразу видны в карусели",
        ) {
            if (form == MedicineForm.TABLET || form == MedicineForm.CAPSULE) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.Md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            "Два цвета",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
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

            MedicineColorPicker(
                title = if (secondaryColorArgb == null) {
                    "Цвет лекарства"
                } else {
                    "Первая половина"
                },
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
                title = "Фон карточки",
                selectedColor = backgroundColorArgb,
                onColorChanged = onBackgroundColorChanged,
                colors = MedicineBackgroundPalette,
            )
        }
    }
}
