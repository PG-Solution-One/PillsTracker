package com.denisp.pillstracker.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.denisp.pillstracker.R
import java.time.LocalDate

@Composable
fun ProfileDatePickerDialog(
    selectedDate: LocalDate?,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onClear: (() -> Unit)? = null,
) {
    AppDatePickerDialog(
        title = stringResource(R.string.birth_date),
        selectedDate = selectedDate,
        onDismiss = onDismiss,
        onDateSelected = onDateSelected,
        maxDate = LocalDate.now(),
        onClear = onClear,
    )
}
