package com.denisp.pillstracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.denisp.pillstracker.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

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
    var showCalendar by rememberSaveable { mutableStateOf(false) }
    val initialText = selectedDate?.format(DateInputFormatter).orEmpty()
    var inputValue by remember(selectedDate) {
        mutableStateOf(
            TextFieldValue(
                text = initialText,
                selection = TextRange(0, initialText.length),
            ),
        )
    }
    var replaceDateOnNextInput by remember { mutableStateOf(true) }
    var openCalendarNextFrame by remember { mutableStateOf(false) }
    val inputDate = parseDateInput(inputValue.text)
    val inputDateInRange = inputDate?.takeIf {
        isDateInRange(it, minDate = minDate, maxDate = maxDate)
    }
    val inputError = when {
        inputValue.text.isBlank() -> null
        inputValue.text.filter(Char::isDigit).length < DATE_INPUT_DIGITS -> null
        inputDate == null -> stringResource(R.string.invalid_date)
        inputDateInRange == null -> stringResource(R.string.date_out_of_range)
        else -> null
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val inputFocusRequester = remember { FocusRequester() }
    val selectedCalendarDate = state.selectedDateMillis?.let(::utcMillisToLocalDate)
    val confirmEnabled = if (showCalendar) {
        selectedCalendarDate != null
    } else {
        inputDateInRange != null
    }

    LaunchedEffect(openCalendarNextFrame) {
        if (openCalendarNextFrame) {
            withFrameNanos { }
            openCalendarNextFrame = false
            showCalendar = true
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(max = DATE_DIALOG_MAX_WIDTH)
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                ) {
                PickerDialogHeader(
                    title = title,
                    actionContentDescription = stringResource(
                        if (showCalendar) {
                            R.string.enter_date_manually
                        } else {
                            R.string.open_calendar
                        },
                    ),
                    onAction = {
                        if (showCalendar) {
                            val calendarText = selectedCalendarDate
                                ?.format(DateInputFormatter)
                                .orEmpty()
                            inputValue = TextFieldValue(
                                text = calendarText,
                                selection = TextRange(0, calendarText.length),
                            )
                            replaceDateOnNextInput = true
                            showCalendar = false
                        } else {
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                            inputDateInRange?.let { date ->
                                val millis = date.toUtcDateMillis()
                                state.selectedDateMillis = millis
                                state.displayedMonthMillis = millis
                            }
                            openCalendarNextFrame = true
                        }
                    },
                    actionIcon = if (showCalendar) {
                        Icons.Rounded.Keyboard
                    } else {
                        Icons.Rounded.CalendarMonth
                    },
                )
                if (showCalendar) {
                    DatePicker(
                        state = state,
                        modifier = Modifier.fillMaxWidth(),
                        title = null,
                        headline = null,
                        showModeToggle = false,
                    )
                } else {
                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = { changed ->
                            val changedText = if (replaceDateOnNextInput) {
                                replacementInput(
                                    previous = inputValue.text,
                                    changed = changed.text,
                                )
                            } else {
                                changed.text
                            }
                            replaceDateOnNextInput = false
                            val formatted = formatDateInput(changedText)
                            inputValue = TextFieldValue(
                                text = formatted,
                                selection = TextRange(formatted.length),
                            )
                            val parsed = parseDateInput(formatted)
                                ?.takeIf {
                                    isDateInRange(
                                        it,
                                        minDate = minDate,
                                        maxDate = maxDate,
                                    )
                                }
                            state.selectedDateMillis = parsed?.toUtcDateMillis()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                            .testTag(DATE_INPUT_TAG)
                            .focusRequester(inputFocusRequester)
                            .onFocusChanged { focus ->
                                if (focus.isFocused) {
                                    replaceDateOnNextInput = true
                                    inputValue = inputValue.copy(
                                        selection = TextRange(0, inputValue.text.length),
                                    )
                                }
                            },
                        label = { Text(stringResource(R.string.date)) },
                        placeholder = { Text(stringResource(R.string.date_input_placeholder)) },
                        supportingText = inputError?.let { error ->
                            { Text(error) }
                        },
                        isError = inputError != null,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                inputDateInRange?.let(onDateSelected)
                            },
                        ),
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    if (selectedDate != null && onClear != null) {
                        TextButton(
                            onClick = onClear,
                            modifier = Modifier.heightIn(min = 48.dp),
                        ) {
                            Text(stringResource(R.string.clear))
                        }
                    }
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                    TextButton(
                        onClick = {
                            val confirmedDate = if (showCalendar) {
                                selectedCalendarDate
                            } else {
                                inputDateInRange
                            }
                            confirmedDate?.let(onDateSelected)
                        },
                        modifier = Modifier.heightIn(min = 48.dp),
                        enabled = confirmEnabled,
                    ) {
                        Text(stringResource(R.string.choose))
                    }
                }
            }
        }
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
    var showDial by rememberSaveable { mutableStateOf(false) }
    val initialHourText = (safeInitialMinute / MINUTES_PER_HOUR).twoDigits()
    val initialMinuteText = (safeInitialMinute % MINUTES_PER_HOUR).twoDigits()
    var hourValue by remember(initialMinuteOfDay) {
        mutableStateOf(
            TextFieldValue(
                initialHourText,
                selection = TextRange(0, initialHourText.length),
            ),
        )
    }
    var minuteValue by remember(initialMinuteOfDay) {
        mutableStateOf(
            TextFieldValue(
                initialMinuteText,
                selection = TextRange(0, initialMinuteText.length),
            ),
        )
    }
    var replaceHourOnNextInput by remember { mutableStateOf(true) }
    var replaceMinuteOnNextInput by remember { mutableStateOf(true) }
    var openDialNextFrame by remember { mutableStateOf(false) }
    var focusMinuteNextFrame by remember { mutableStateOf(false) }
    val inputHour = hourValue.text.toIntOrNull()?.takeIf { it in 0 until HOURS_PER_DAY }
    val inputMinute = minuteValue.text.toIntOrNull()?.takeIf { it in 0 until MINUTES_PER_HOUR }
    val hourFocusRequester = remember { FocusRequester() }
    val minuteFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(openDialNextFrame) {
        if (openDialNextFrame) {
            withFrameNanos { }
            openDialNextFrame = false
            showDial = true
        }
    }

    LaunchedEffect(focusMinuteNextFrame) {
        if (focusMinuteNextFrame) {
            withFrameNanos { }
            focusMinuteNextFrame = false
            replaceMinuteOnNextInput = true
            minuteFocusRequester.requestFocus()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PickerDialogHeader(
                title = title,
                actionContentDescription = stringResource(
                    if (showDial) {
                        R.string.enter_time_manually
                    } else {
                        R.string.open_clock
                    },
                ),
                onAction = {
                    if (showDial) {
                        val hourText = state.hour.twoDigits()
                        val minuteText = state.minute.twoDigits()
                        hourValue = TextFieldValue(
                            hourText,
                            selection = TextRange(0, hourText.length),
                        )
                        minuteValue = TextFieldValue(
                            minuteText,
                            selection = TextRange(0, minuteText.length),
                        )
                        replaceHourOnNextInput = true
                        replaceMinuteOnNextInput = true
                        showDial = false
                    } else {
                        focusManager.clearFocus(force = true)
                        keyboardController?.hide()
                        openDialNextFrame = true
                    }
                },
                actionIcon = if (showDial) {
                    Icons.Rounded.Keyboard
                } else {
                    Icons.Rounded.Schedule
                },
            )
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = TIME_PICKER_MAX_WIDTH)
                        .fillMaxWidth(),
                ) {
                    if (showDial) {
                        TimePicker(
                            state = state,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(TIME_DIAL_TAG),
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            OutlinedTextField(
                            value = hourValue,
                            onValueChange = { changed ->
                                val changedText = if (replaceHourOnNextInput) {
                                    replacementInput(
                                        previous = hourValue.text,
                                        changed = changed.text,
                                    )
                                } else {
                                    changed.text
                                }
                                replaceHourOnNextInput = false
                                val digits = validatedTimeInput(
                                    value = changedText,
                                    maxExclusive = HOURS_PER_DAY,
                                )
                                if (digits != null) {
                                    val hour = digits.toIntOrNull()
                                    hourValue = TextFieldValue(
                                        text = digits,
                                        selection = TextRange(digits.length),
                                    )
                                    hour?.let { state.hour = it }
                                    if (digits.length == TIME_INPUT_DIGITS) {
                                        focusMinuteNextFrame = true
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag(TIME_HOUR_INPUT_TAG)
                                .focusRequester(hourFocusRequester)
                                .onFocusChanged { focus ->
                                    if (focus.isFocused) {
                                        replaceHourOnNextInput = true
                                        hourValue = hourValue.copy(
                                            selection = TextRange(0, hourValue.text.length),
                                        )
                                    }
                                },
                            label = { Text(stringResource(R.string.hours)) },
                            placeholder = { Text(stringResource(R.string.hours_placeholder)) },
                            isError = hourValue.text.isNotEmpty() && inputHour == null,
                            singleLine = true,
                            textStyle = MaterialTheme.typography.headlineLarge.copy(
                                textAlign = TextAlign.Center,
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next,
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { minuteFocusRequester.requestFocus() },
                            ),
                            )
                            Text(
                                text = ":",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            OutlinedTextField(
                            value = minuteValue,
                            onValueChange = { changed ->
                                val changedText = if (replaceMinuteOnNextInput) {
                                    replacementInput(
                                        previous = minuteValue.text,
                                        changed = changed.text,
                                    )
                                } else {
                                    changed.text
                                }
                                replaceMinuteOnNextInput = false
                                val digits = validatedTimeInput(
                                    value = changedText,
                                    maxExclusive = MINUTES_PER_HOUR,
                                )
                                if (digits != null) {
                                    val minute = digits.toIntOrNull()
                                    minuteValue = TextFieldValue(
                                        text = digits,
                                        selection = TextRange(digits.length),
                                    )
                                    minute?.let { state.minute = it }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag(TIME_MINUTE_INPUT_TAG)
                                .focusRequester(minuteFocusRequester)
                                .onFocusChanged { focus ->
                                    if (focus.isFocused) {
                                        replaceMinuteOnNextInput = true
                                        minuteValue = minuteValue.copy(
                                            selection = TextRange(0, minuteValue.text.length),
                                        )
                                    }
                                },
                            label = { Text(stringResource(R.string.minutes)) },
                            placeholder = { Text(stringResource(R.string.minutes_placeholder)) },
                            isError = minuteValue.text.isNotEmpty() && inputMinute == null,
                            singleLine = true,
                            textStyle = MaterialTheme.typography.headlineLarge.copy(
                                textAlign = TextAlign.Center,
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (inputHour != null && inputMinute != null) {
                                        onTimeSelected(minuteOfDay(inputHour, inputMinute))
                                    }
                                },
                            ),
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
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
                        val selectedMinute = if (showDial) {
                            minuteOfDay(state.hour, state.minute)
                        } else {
                            inputHour?.let { hour ->
                                inputMinute?.let { minute -> minuteOfDay(hour, minute) }
                            }
                        }
                        selectedMinute?.let(onTimeSelected)
                    },
                    modifier = Modifier.heightIn(min = 48.dp),
                    enabled = showDial || inputHour != null && inputMinute != null,
                ) {
                    Text(stringResource(R.string.done))
                }
            }
        }
    }
}

@Composable
private fun PickerDialogHeader(
    title: String,
    actionContentDescription: String,
    onAction: () -> Unit,
    actionIcon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = 24.dp,
                top = 16.dp,
                end = 12.dp,
                bottom = 8.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        IconButton(
            onClick = onAction,
            modifier = Modifier.heightIn(min = 48.dp),
        ) {
            Icon(
                imageVector = actionIcon,
                contentDescription = actionContentDescription,
            )
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

internal fun formatDateInput(value: String): String {
    val allDigits = value.filter(Char::isDigit)
    val digits = if (allDigits.length > DATE_INPUT_DIGITS) {
        allDigits.takeLast(DATE_INPUT_DIGITS)
    } else {
        allDigits
    }
    return buildString {
        digits.forEachIndexed { index, digit ->
            if (index == 2 || index == 4) append('.')
            append(digit)
        }
    }
}

internal fun parseDateInput(value: String): LocalDate? {
    val digits = value.filter(Char::isDigit)
    if (digits.length != DATE_INPUT_DIGITS) return null
    return runCatching {
        LocalDate.of(
            digits.substring(4, 8).toInt(),
            digits.substring(2, 4).toInt(),
            digits.substring(0, 2).toInt(),
        )
    }.getOrNull()
}

internal fun normalizeTimeInput(value: String): String {
    val digits = value.filter(Char::isDigit)
    return if (digits.length > TIME_INPUT_DIGITS) {
        digits.takeLast(TIME_INPUT_DIGITS)
    } else {
        digits
    }
}

internal fun validatedTimeInput(value: String, maxExclusive: Int): String? {
    val digits = normalizeTimeInput(value)
    if (digits.isEmpty()) return digits
    return digits.takeIf {
        it.toIntOrNull()?.let { number -> number in 0 until maxExclusive } == true
    }
}

internal fun replacementInput(previous: String, changed: String): String {
    val previousDigits = previous.filter(Char::isDigit)
    val changedDigits = changed.filter(Char::isDigit)
    return when {
        changedDigits.length <= previousDigits.length -> changedDigits
        changedDigits.startsWith(previousDigits) -> changedDigits.drop(previousDigits.length)
        changedDigits.endsWith(previousDigits) -> changedDigits.dropLast(previousDigits.length)
        else -> changedDigits
    }
}

private fun Int.twoDigits(): String = toString().padStart(2, '0')

private val DateInputFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.uuuu")
internal const val DATE_INPUT_TAG = "date-input"
internal const val TIME_HOUR_INPUT_TAG = "time-hour-input"
internal const val TIME_MINUTE_INPUT_TAG = "time-minute-input"
internal const val TIME_DIAL_TAG = "time-dial"
private val DATE_DIALOG_MAX_WIDTH = 400.dp
private val TIME_PICKER_MAX_WIDTH = 480.dp
private const val DATE_INPUT_DIGITS = 8
private const val TIME_INPUT_DIGITS = 2
private const val MINUTES_PER_HOUR = 60
private const val HOURS_PER_DAY = 24
private const val MINUTES_PER_DAY = HOURS_PER_DAY * MINUTES_PER_HOUR
