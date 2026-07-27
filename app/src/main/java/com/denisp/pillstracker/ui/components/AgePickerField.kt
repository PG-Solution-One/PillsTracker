package com.denisp.pillstracker.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.denisp.pillstracker.R
import com.denisp.pillstracker.ui.theme.AppPickerField
import java.time.LocalDate
import java.time.Period

fun calculateAge(
    birthDate: LocalDate,
    today: LocalDate = LocalDate.now(),
): Int = Period.between(birthDate, today).years.coerceAtLeast(0)

@Composable
fun AgePickerField(
    birthDate: LocalDate?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    optional: Boolean = false,
) {
    val age = birthDate?.let(::calculateAge) ?: -1
    val value = when {
        birthDate == null && optional -> stringResource(R.string.age_not_set_optional)
        birthDate == null -> stringResource(R.string.age_not_set)
        age == 0 -> stringResource(R.string.age_less_than_year)
        else -> pluralStringResource(R.plurals.age_years, age, age)
    }
    AppPickerField(
        label = stringResource(R.string.age),
        value = value,
        onClick = onClick,
        modifier = modifier,
        leadingIcon = Icons.Rounded.Cake,
    )
}
