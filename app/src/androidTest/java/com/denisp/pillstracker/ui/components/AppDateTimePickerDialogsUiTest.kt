package com.denisp.pillstracker.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDateTimePickerDialogsUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun datePickerShowsInputAndCalendarWithoutInitialFocus() {
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
            .onNodeWithTag(DATE_INPUT_TAG)
            .assertIsDisplayed()
            .assertIsNotFocused()
        composeRule
            .onNodeWithTag(DATE_CALENDAR_TAG)
            .assertIsDisplayed()
    }

    @Test
    fun timePickerShowsCombinedInputAndDialWithoutInitialFocus() {
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
            .onNodeWithTag(TIME_INPUT_TAG)
            .assertIsDisplayed()
            .assertIsNotFocused()
            .assertTextEquals("Время", "08:30")
        composeRule
            .onNodeWithTag(TIME_DIAL_TAG)
            .assertIsDisplayed()
    }

    @Test
    fun keyboardDoneConfirmsManuallyEnteredDate() {
        val selectedDate = AtomicReference<LocalDate>()
        composeRule.setContent {
            MaterialTheme {
                AppDatePickerDialog(
                    title = "Дата начала курса",
                    selectedDate = null,
                    onDismiss = {},
                    onDateSelected = selectedDate::set,
                )
            }
        }

        composeRule
            .onNodeWithTag(DATE_INPUT_TAG)
            .performTextReplacement("15082026")
        composeRule
            .onNodeWithTag(DATE_INPUT_TAG)
            .performImeAction()

        composeRule.runOnIdle {
            assertEquals(LocalDate.of(2026, 8, 15), selectedDate.get())
        }
    }

    @Test
    fun keyboardDoneConfirmsManuallyEnteredTime() {
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
            .onNodeWithTag(TIME_INPUT_TAG)
            .performTextReplacement("2145")
        composeRule
            .onNodeWithTag(TIME_INPUT_TAG)
            .performImeAction()

        composeRule.runOnIdle {
            assertEquals(21 * 60 + 45, selectedMinute.get())
        }
    }

    @Test
    fun touchingTimeDialClearsManualInputFocus() {
        composeRule.setContent {
            MaterialTheme {
                AppTimePickerDialog(
                    title = "Время приёма",
                    initialMinuteOfDay = 15 * 60 + 7,
                    onDismiss = {},
                    onTimeSelected = {},
                )
            }
        }

        composeRule
            .onNodeWithTag(TIME_INPUT_TAG)
            .performClick()
        composeRule
            .onNodeWithTag(TIME_INPUT_TAG)
            .assertIsFocused()
        composeRule
            .onNodeWithTag(TIME_DIAL_TAG)
            .performTouchInput { click() }
            .assertIsDisplayed()
        composeRule
            .onNodeWithTag(TIME_INPUT_TAG)
            .assertIsNotFocused()
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
            .onNodeWithTag(TIME_INPUT_TAG)
            .assertIsDisplayed()
            .performTextReplacement("2145")
        composeRule
            .onNodeWithTag(TIME_INPUT_TAG)
            .assertTextEquals("Время", "21:45")
        composeRule
            .onNodeWithText("Готово")
            .performClick()

        composeRule.runOnIdle {
            assertEquals(21 * 60 + 45, selectedMinute.get())
        }
    }

    @Test
    fun invalidCombinedTimeDisablesConfirmation() {
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
            .onNodeWithTag(TIME_INPUT_TAG)
            .performTextReplacement("2960")
        composeRule
            .onNodeWithTag(TIME_INPUT_TAG)
            .assertTextEquals("Время", "29:60")
        composeRule
            .onNodeWithText("Готово")
            .assertIsNotEnabled()
    }
}
