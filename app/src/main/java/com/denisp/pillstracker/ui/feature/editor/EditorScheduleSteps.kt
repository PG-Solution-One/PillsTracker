package com.denisp.pillstracker.ui.feature.editor

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.model.ScheduleKind
import com.denisp.pillstracker.model.dayMask
import com.denisp.pillstracker.ui.DateFormatter
import com.denisp.pillstracker.ui.TimeFormatter
import java.time.LocalDate
import java.time.LocalTime

@Composable
internal fun CourseStep(
    startDate: LocalDate,
    onPickStartDate: () -> Unit,
    endMode: CourseEndMode,
    onEndModeChanged: (CourseEndMode) -> Unit,
    endDate: LocalDate,
    onPickEndDate: () -> Unit,
    courseDays: String,
    onCourseDaysChanged: (String) -> Unit,
    scheduleKind: ScheduleKind,
    onScheduleKindChanged: (ScheduleKind) -> Unit,
    showError: Boolean,
) {
    DateButton("Дата начала", startDate, onPickStartDate)
    SelectionField(
        "Продолжительность курса",
        endMode,
        CourseEndMode.entries,
        onEndModeChanged,
        CourseEndMode::title,
    )
    when (endMode) {
        CourseEndMode.WITHOUT_END -> Unit
        CourseEndMode.END_DATE -> DateButton("Дата окончания", endDate, onPickEndDate)
        CourseEndMode.DAYS_COUNT -> OutlinedTextField(
            value = courseDays,
            onValueChange = onCourseDaysChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Количество дней") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            isError = showError && (courseDays.toLongOrNull() ?: 0) <= 0,
        )
    }
    HorizontalDivider()
    SelectionField(
        "Схема приёма",
        scheduleKind,
        ScheduleKind.entries,
        onScheduleKindChanged,
        ScheduleKind::title,
    )
    if (scheduleKind == ScheduleKind.EVERY_OTHER_DAY) {
        Text(
            "Отсчёт «через день» начинается с даты начала курса.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun TimeStep(
    scheduleKind: ScheduleKind,
    times: List<EditableScheduleTime>,
    onAdd: () -> Unit,
    onChangeTime: (Int) -> Unit,
    onToggleDay: (Int, Int) -> Unit,
    onRemove: (Int) -> Unit,
    showError: Boolean,
) {
    if (scheduleKind == ScheduleKind.AS_NEEDED) {
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Без фиксированного времени", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Приём можно будет отметить вручную на главном экране.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        return
    }

    times.forEachIndexed { index, schedule ->
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(onClick = { onChangeTime(index) }) {
                        Text(
                            LocalTime.of(schedule.minuteOfDay / 60, schedule.minuteOfDay % 60)
                                .format(TimeFormatter),
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                    if (times.size > 1) {
                        TextButton(onClick = { onRemove(index) }) { Text("Удалить") }
                    }
                }
                if (scheduleKind == ScheduleKind.SELECTED_DAYS) {
                    Text("Дни для этого времени", fontWeight = FontWeight.Medium)
                    DaySelector(schedule.dayMask) { day -> onToggleDay(index, day) }
                    if (showError && schedule.dayMask == 0) {
                        Text("Выберите хотя бы один день", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
    OutlinedButton(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
        Text("+ Добавить время")
    }
}

@Composable
private fun DaySelector(mask: Int, onToggle: (Int) -> Unit) {
    val labels = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        labels.forEachIndexed { index, label ->
            val selected = mask and dayMask(index + 1) != 0
            Surface(
                color = if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                },
                shape = CircleShape,
                modifier = Modifier.clickable { onToggle(index + 1) },
            ) {
                Text(
                    label,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun DateButton(label: String, date: LocalDate, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label)
            Text(date.format(DateFormatter), fontWeight = FontWeight.SemiBold)
        }
    }
}

internal fun showDatePicker(
    context: Context,
    initial: LocalDate,
    onSelected: (LocalDate) -> Unit,
) {
    DatePickerDialog(
        context,
        { _, year, month, day -> onSelected(LocalDate.of(year, month + 1, day)) },
        initial.year,
        initial.monthValue - 1,
        initial.dayOfMonth,
    ).show()
}

internal fun showTimePicker(
    context: Context,
    initialMinute: Int,
    onSelected: (Int) -> Unit,
) {
    TimePickerDialog(
        context,
        { _, hour, minute -> onSelected(hour * 60 + minute) },
        initialMinute / 60,
        initialMinute % 60,
        true,
    ).show()
}
