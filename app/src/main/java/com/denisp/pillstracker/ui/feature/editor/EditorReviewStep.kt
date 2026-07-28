package com.denisp.pillstracker.ui.feature.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.model.MealTiming
import com.denisp.pillstracker.model.ScheduleKind
import com.denisp.pillstracker.model.displayAmount
import com.denisp.pillstracker.ui.DateFormatter
import com.denisp.pillstracker.ui.theme.AppSpacing
import com.denisp.pillstracker.ui.theme.AppTextField
import java.time.LocalDate

@Composable
internal fun DetailsStep(
    mealTiming: MealTiming,
    onMealTimingChanged: (MealTiming) -> Unit,
    note: String,
    onNoteChanged: (String) -> Unit,
    name: String,
    dosage: String,
    tabletsPerIntake: Double,
    scheduleKind: ScheduleKind,
    times: List<EditableScheduleTime>,
    startDate: LocalDate,
    endDate: LocalDate?,
) {
    EditorStepContent {
        EditorSectionCard(
            title = "Инструкция",
            supportingText = "Дополнительные условия приёма лекарства",
        ) {
            SelectionField(
                label = "Связь с едой",
                selected = mealTiming,
                options = MealTiming.entries,
                onSelected = onMealTimingChanged,
                title = MealTiming::title,
            )
            AppTextField(
                value = note,
                onValueChange = { onNoteChanged(limitMedicineNote(it)) },
                label = "Заметка",
                modifier = Modifier.heightIn(min = 130.dp),
                placeholder = "Например, запивать стаканом воды",
                singleLine = false,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                ),
                minLines = 3,
                maxLines = 4,
                supportingText = {
                    Text(
                        text = "${medicineNoteLength(note)} / $MAX_MEDICINE_NOTE_LENGTH",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                    )
                },
            )
        }

        EditorSectionCard(
            title = "Проверьте назначение",
            supportingText = "Эти данные будут использованы для расписания и напоминаний",
        ) {
            SummaryLine("Лекарство", name)
            SummaryLine("Дозировка", "$dosage · ${tabletsPerIntake.displayAmount()} шт.")
            SummaryLine(
                "Курс",
                if (endDate == null) {
                    "с ${startDate.format(DateFormatter)}, без окончания"
                } else {
                    "${startDate.format(DateFormatter)} — ${endDate.format(DateFormatter)}"
                },
            )
            SummaryLine(
                "Расписание",
                if (scheduleKind == ScheduleKind.AS_NEEDED) {
                    scheduleKind.title
                } else {
                    "${scheduleKind.title}, ${times.size} раз(а) в день"
                },
            )
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.Lg),
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(0.38f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            modifier = Modifier.weight(0.62f),
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
        )
    }
}
