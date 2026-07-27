package com.denisp.pillstracker.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
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
        composeRule
            .onNodeWithTag(DATE_INPUT_TAG)
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

    @Test
    fun existingTimeCanBeOverwrittenWithoutClearing() {
        val selectedMinute = AtomicInteger(-1)
        composeRule.setContent {
            MaterialTheme {
                AppTimePickerDialog(
                    title = "Время приёма",
                    initialMinuteOfDay = 8 * 60 + 30,
                    onDismiss = {},
                    onTimeSelected = selectedMinute::set,
                )
            }
        }

        composeRule
            .onNodeWithTag(TIME_HOUR_INPUT_TAG)
            .assertIsDisplayed()
            .performTextReplacement("21")
        composeRule
            .onNodeWithTag(TIME_HOUR_INPUT_TAG)
            .assertTextEquals("Часы", "21")
        composeRule
            .onNodeWithTag(TIME_MINUTE_INPUT_TAG)
            .performTextReplacement("45")
        composeRule
            .onNodeWithTag(TIME_MINUTE_INPUT_TAG)
            .assertTextEquals("Минуты", "45")
        composeRule
            .onNodeWithText("Готово")
            .performClick()

        composeRule.runOnIdle {
            assertEquals(21 * 60 + 45, selectedMinute.get())
        }
    }

    @Test
    fun timeInputRejectsImpossibleValuesAndMovesFocusToMinutes() {
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
            .onNodeWithTag(TIME_HOUR_INPUT_TAG)
            .performTextReplacement("29")
        composeRule
            .onNodeWithTag(TIME_HOUR_INPUT_TAG)
            .assertTextEquals("Часы", "08")
            .performTextReplacement("23")
        composeRule
            .onNodeWithTag(TIME_MINUTE_INPUT_TAG)
            .assertIsFocused()
            .performTextReplacement("99")
        composeRule
            .onNodeWithTag(TIME_MINUTE_INPUT_TAG)
            .assertTextEquals("Минуты", "30")
    }
}
