package com.denisp.pillstracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.denisp.pillstracker.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerDialog(
    title: String,
    selectedDate: LocalDate?,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    onClear: (() -> Unit)? = null,
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate?.toUtcDateMillis(),
        initialDisplayedMonthMillis = selectedDate?.toUtcDateMillis(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                isDateInRange(
                    date = utcMillisToLocalDate(utcTimeMillis),
                    minDate = minDate,
                    maxDate = maxDate,
                )

            override fun isSelectableYear(year: Int): Boolean =
                (minDate == null || year >= minDate.year) &&
                    (maxDate == null || year <= maxDate.year)
        },
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis
                        ?.let(::utcMillisToLocalDate)
                        ?.let(onDateSelected)
                },
                modifier = Modifier.heightIn(min = 48.dp),
                enabled = state.selectedDateMillis != null,
            ) {
                Text(stringResource(R.string.choose))
            }
        },
        dismissButton = {
            if (selectedDate != null && onClear != null) {
                TextButton(
                    onClick = onClear,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(stringResource(R.string.clear))
                }
            } else {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        },
    ) {
        Column(Modifier.fillMaxWidth()) {
            Text(
                text = title,
                modifier = Modifier.padding(
                    start = 24.dp,
                    top = 24.dp,
                    end = 24.dp,
                    bottom = 8.dp,
                ),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            DatePicker(
                state = state,
                title = null,
                headline = null,
                showModeToggle = false,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTimePickerDialog(
    title: String,
    initialMinuteOfDay: Int,
    onDismiss: () -> Unit,
    onTimeSelected: (Int) -> Unit,
) {
    val safeInitialMinute = initialMinuteOfDay.coerceIn(0, MINUTES_PER_DAY - 1)
    val state = rememberTimePickerState(
        initialHour = safeInitialMinute / MINUTES_PER_HOUR,
        initialMinute = safeInitialMinute % MINUTES_PER_HOUR,
        is24Hour = true,
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.widthIn(min = 280.dp, max = 360.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text(
                    text = title,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                TimePicker(state = state)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                    TextButton(
                        onClick = {
                            onTimeSelected(
                                minuteOfDay(
                                    hour = state.hour,
                                    minute = state.minute,
                                ),
                            )
                        },
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) {
                        Text(stringResource(R.string.done))
                    }
                }
            }
        }
    }
}

internal fun LocalDate.toUtcDateMillis(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

internal fun utcMillisToLocalDate(utcTimeMillis: Long): LocalDate =
    Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate()

internal fun isDateInRange(
    date: LocalDate,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
): Boolean =
    (minDate == null || !date.isBefore(minDate)) &&
        (maxDate == null || !date.isAfter(maxDate))

internal fun minuteOfDay(hour: Int, minute: Int): Int =
    hour.coerceIn(0, HOURS_PER_DAY - 1) * MINUTES_PER_HOUR +
        minute.coerceIn(0, MINUTES_PER_HOUR - 1)

private const val MINUTES_PER_HOUR = 60
private const val HOURS_PER_DAY = 24
private const val MINUTES_PER_DAY = HOURS_PER_DAY * MINUTES_PER_HOUR
