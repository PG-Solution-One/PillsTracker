package com.denisp.pillstracker.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
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
    val initialText = selectedDate?.format(DateInputFormatter).orEmpty()
    var inputValue by remember(selectedDate) {
        mutableStateOf(
            TextFieldValue(
                text = initialText,
                selection = TextRange(initialText.length),
            ),
        )
    }
    var replaceDateOnNextInput by remember { mutableStateOf(true) }
    var inputFocused by remember { mutableStateOf(false) }
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
    val selectedCalendarDate = state.selectedDateMillis?.let(::utcMillisToLocalDate)
    val confirmEnabled = inputDateInRange != null
    val submitDateInput: () -> Unit = {
        inputDateInRange?.let { date ->
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
            onDateSelected(date)
        }
        Unit
    }

    LaunchedEffect(state.selectedDateMillis, inputFocused) {
        if (!inputFocused) {
            selectedCalendarDate?.let { date ->
                val calendarText = date.format(DateInputFormatter)
                inputValue = TextFieldValue(
                    text = calendarText,
                    selection = TextRange(calendarText.length),
                )
            }
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
                        .imePadding()
                        .verticalScroll(rememberScrollState()),
                ) {
                    PickerDialogHeader(title = title)
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
                            parsed?.let { date ->
                                val millis = date.toUtcDateMillis()
                                state.selectedDateMillis = millis
                                state.displayedMonthMillis = millis
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                            .testTag(DATE_INPUT_TAG)
                            .onPreviewKeyEvent { event ->
                                if (event.key == Key.Enter && event.type == KeyEventType.KeyDown) {
                                    submitDateInput()
                                    true
                                } else {
                                    false
                                }
                            }
                            .onFocusChanged { focus ->
                                inputFocused = focus.isFocused
                                if (focus.isFocused) {
                                    replaceDateOnNextInput = inputValue.text.isNotEmpty()
                                    inputValue = inputValue.copy(
                                        selection = TextRange(inputValue.text.length),
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
                            onDone = { submitDateInput() },
                        ),
                    )
                    DatePicker(
                        state = state,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(DATE_CALENDAR_TAG)
                            .clearFocusOnPointerDown(
                                focusManager = focusManager,
                                hideKeyboard = { keyboardController?.hide() },
                            ),
                        title = null,
                        headline = null,
                        showModeToggle = false,
                    )
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
                            onClick = submitDateInput,
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
    val initialTimeText = formatTime(
        hour = safeInitialMinute / MINUTES_PER_HOUR,
        minute = safeInitialMinute % MINUTES_PER_HOUR,
    )
    var timeValue by remember(initialMinuteOfDay) {
        mutableStateOf(
            TextFieldValue(
                initialTimeText,
                selection = TextRange(initialTimeText.length),
            ),
        )
    }
    var replaceTimeOnNextInput by remember { mutableStateOf(true) }
    var inputFocused by remember { mutableStateOf(false) }
    val inputTime = parseTimeInput(timeValue.text)
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val submitTimeInput: () -> Unit = {
        inputTime?.let { minuteOfDay ->
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
            onTimeSelected(minuteOfDay)
        }
        Unit
    }

    LaunchedEffect(state.hour, state.minute, inputFocused) {
        if (!inputFocused) {
            val dialText = formatTime(state.hour, state.minute)
            timeValue = TextFieldValue(
                text = dialText,
                selection = TextRange(dialText.length),
            )
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
                .imePadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PickerDialogHeader(title = title)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = TIME_PICKER_MAX_WIDTH)
                        .fillMaxWidth(),
                ) {
                    OutlinedTextField(
                        value = timeValue,
                        onValueChange = { changed ->
                            val changedText = if (replaceTimeOnNextInput) {
                                replacementInput(
                                    previous = timeValue.text,
                                    changed = changed.text,
                                )
                            } else {
                                changed.text
                            }
                            replaceTimeOnNextInput = false
                            val formatted = formatTimeInput(changedText)
                            timeValue = TextFieldValue(
                                text = formatted,
                                selection = TextRange(formatted.length),
                            )
                            parseTimeInput(formatted)?.let { minuteOfDay ->
                                state.hour = minuteOfDay / MINUTES_PER_HOUR
                                state.minute = minuteOfDay % MINUTES_PER_HOUR
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                            .testTag(TIME_INPUT_TAG)
                            .onPreviewKeyEvent { event ->
                                if (event.key == Key.Enter && event.type == KeyEventType.KeyDown) {
                                    submitTimeInput()
                                    true
                                } else {
                                    false
                                }
                            }
                            .onFocusChanged { focus ->
                                inputFocused = focus.isFocused
                                if (focus.isFocused) {
                                    replaceTimeOnNextInput = timeValue.text.isNotEmpty()
                                    timeValue = timeValue.copy(
                                        selection = TextRange(timeValue.text.length),
                                    )
                                }
                            },
                        label = { Text(stringResource(R.string.time)) },
                        placeholder = { Text(stringResource(R.string.time_input_placeholder)) },
                        supportingText = if (
                            timeValue.text.filter(Char::isDigit).length == TIME_INPUT_DIGITS &&
                            inputTime == null
                        ) {
                            { Text(stringResource(R.string.invalid_time)) }
                        } else {
                            null
                        },
                        isError =
                            timeValue.text.filter(Char::isDigit).length == TIME_INPUT_DIGITS &&
                                inputTime == null,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.headlineLarge.copy(
                            textAlign = TextAlign.Center,
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { submitTimeInput() },
                        ),
                    )
                    TimePicker(
                        state = state,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(TIME_DIAL_TAG)
                            .clearFocusOnPointerDown(
                                focusManager = focusManager,
                                hideKeyboard = { keyboardController?.hide() },
                            ),
                    )
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
                    onClick = submitTimeInput,
                    modifier = Modifier.heightIn(min = 48.dp),
                    enabled = inputTime != null,
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
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = 24.dp,
                top = 16.dp,
                end = 24.dp,
                bottom = 8.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
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

internal fun formatTimeInput(value: String): String {
    val allDigits = value.filter(Char::isDigit)
    val digits = if (allDigits.length > TIME_INPUT_DIGITS) {
        allDigits.takeLast(TIME_INPUT_DIGITS)
    } else {
        allDigits
    }
    return buildString {
        digits.forEachIndexed { index, digit ->
            if (index == 2) append(':')
            append(digit)
        }
    }
}

internal fun parseTimeInput(value: String): Int? {
    val digits = value.filter(Char::isDigit)
    if (digits.length != TIME_INPUT_DIGITS) return null
    val hour = digits.substring(0, 2).toInt()
    val minute = digits.substring(2, 4).toInt()
    if (hour !in 0 until HOURS_PER_DAY || minute !in 0 until MINUTES_PER_HOUR) return null
    return minuteOfDay(hour, minute)
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

private fun formatTime(hour: Int, minute: Int): String =
    "${hour.twoDigits()}:${minute.twoDigits()}"

private fun Modifier.clearFocusOnPointerDown(
    focusManager: FocusManager,
    hideKeyboard: () -> Unit,
): Modifier = pointerInput(focusManager) {
    awaitEachGesture {
        awaitFirstDown(pass = PointerEventPass.Initial)
        focusManager.clearFocus(force = true)
        hideKeyboard()
    }
}

private val DateInputFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.uuuu")
internal const val DATE_INPUT_TAG = "date-input"
internal const val DATE_CALENDAR_TAG = "date-calendar"
internal const val TIME_INPUT_TAG = "time-input"
internal const val TIME_DIAL_TAG = "time-dial"
private val DATE_DIALOG_MAX_WIDTH = 400.dp
private val TIME_PICKER_MAX_WIDTH = 480.dp
private const val DATE_INPUT_DIGITS = 8
private const val TIME_INPUT_DIGITS = 4
private const val MINUTES_PER_HOUR = 60
private const val HOURS_PER_DAY = 24
private const val MINUTES_PER_DAY = HOURS_PER_DAY * MINUTES_PER_HOUR
