package com.denisp.pillstracker.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDateTimePickerDialogsUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun datePickerStartsWithManualInputAndSwitchesModes() {
        composeRule.setContent {
            MaterialTheme {
                AppDatePickerDialog(
                    title = "Дата начала курса",
                    selectedDate = LocalDate.of(2026, 7, 28),
                    onDismiss = {},
                    onDateSelected = {},
                )
            }
        }

        composeRule
            .onNodeWithContentDescription("Открыть календарь")
            .assertIsDisplayed()
            .performClick()
        composeRule
            .onNodeWithContentDescription("Ввести дату вручную")
            .assertIsDisplayed()
            .performClick()
        composeRule
            .onNodeWithContentDescription("Открыть календарь")
            .assertIsDisplayed()
    }

    @Test
    fun timePickerStartsWithManualInputAndSwitchesModes() {
        composeRule.setContent {
            MaterialTheme {
                AppTimePickerDialog(
                    title = "Время приёма",
                    initialMinuteOfDay = 8 * 60 + 30,
                    onDismiss = {},
                    onTimeSelected = {},
                )
            }
        }

        composeRule
            .onNodeWithContentDescription("Открыть циферблат")
            .assertIsDisplayed()
            .performClick()
        composeRule
            .onNodeWithContentDescription("Ввести время вручную")
            .assertIsDisplayed()
            .performClick()
        composeRule
            .onNodeWithContentDescription("Открыть циферблат")
            .assertIsDisplayed()
    }
}
