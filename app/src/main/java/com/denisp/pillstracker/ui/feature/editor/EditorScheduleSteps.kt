package com.denisp.pillstracker.ui.feature.editor

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onChangeTime(index) }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            if (times.size > 1) "Приём ${index + 1}" else "Время приёма",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Text(
                            LocalTime.of(schedule.minuteOfDay / 60, schedule.minuteOfDay % 60)
                                .format(TimeFormatter),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        if (scheduleKind != ScheduleKind.SELECTED_DAYS) {
                            Text(
                                scheduleKind.title,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                    IconButton(onClick = { onChangeTime(index) }) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Изменить время")
                    }
                    if (times.size > 1) {
                        IconButton(onClick = { onRemove(index) }) {
                            Icon(
                                Icons.Rounded.DeleteOutline,
                                contentDescription = "Удалить время",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
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
    Row(modifier = Modifier.fillMaxWidth()) {
        labels.forEachIndexed { index, label ->
            val selected = mask and dayMask(index + 1) != 0
            Surface(
                color = if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                },
                shape = CircleShape,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp)
                    .clickable { onToggle(index + 1) },
            ) {
                Text(
                    label,
                    modifier = Modifier.padding(vertical = 9.dp),
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
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
