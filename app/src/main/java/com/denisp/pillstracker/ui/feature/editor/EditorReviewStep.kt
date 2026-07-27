package com.denisp.pillstracker.ui.feature.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.model.MealTiming
import com.denisp.pillstracker.model.ScheduleKind
import com.denisp.pillstracker.model.displayAmount
import com.denisp.pillstracker.ui.DateFormatter
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
    SelectionField(
        "Связь с едой",
        mealTiming,
        MealTiming.entries,
        onMealTimingChanged,
        MealTiming::title,
    )
    OutlinedTextField(
        value = note,
        onValueChange = onNoteChanged,
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        label = { Text("Заметка") },
        placeholder = { Text("Например, запивать стаканом воды") },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
        ),
    )
    Card(shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Проверьте назначение",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
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
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 16.dp))
    }
}
