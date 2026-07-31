package com.denisp.pillstracker.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.denisp.pillstracker.model.DosageUnit
import com.denisp.pillstracker.model.IntakeStatus
import com.denisp.pillstracker.model.MealTiming
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.model.MedicineForm
import com.denisp.pillstracker.model.ScheduleKind
import com.denisp.pillstracker.model.ScheduledDose
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MedicineReminderOverlayUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun closeDismissesWithoutChangingStatus() {
        val dismissCount = AtomicInteger()
        val statusCount = AtomicInteger()

        composeRule.setContent {
            MaterialTheme {
                MedicineReminderOverlay(
                    doses = listOf(dose()),
                    onStatus = { _, _ -> statusCount.incrementAndGet() },
                    onTakeAll = { statusCount.incrementAndGet() },
                    onSnooze = {},
                    onDismiss = { dismissCount.incrementAndGet() },
                )
            }
        }

        composeRule.onNodeWithContentDescription("Закрыть напоминание").performClick()
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            assertEquals(1, dismissCount.get())
            assertEquals(0, statusCount.get())
        }
    }

    @Test
    fun skippedFeedbackIsVisibleBeforeCallbackClosesOverlay() {
        val statusCount = AtomicInteger()
        composeRule.mainClock.autoAdvance = false

        composeRule.setContent {
            MaterialTheme {
                MedicineReminderOverlay(
                    doses = listOf(dose()),
                    onStatus = { _, status ->
                        if (status == IntakeStatus.SKIPPED) statusCount.incrementAndGet()
                    },
                    onTakeAll = {},
                    onSnooze = {},
                    onDismiss = {},
                )
            }
        }

        composeRule
            .onNodeWithContentDescription("Отметить как пропущено: Аспирин")
            .performClick()
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Отмечено как пропущено").assertIsDisplayed()
        composeRule.runOnIdle { assertEquals(0, statusCount.get()) }

        composeRule.mainClock.advanceTimeBy(401)
        composeRule.waitForIdle()
        composeRule.runOnIdle { assertEquals(1, statusCount.get()) }
    }

    private fun dose() = ScheduledDose(
        medicine = Medicine(
            id = 1,
            name = "Аспирин",
            form = MedicineForm.TABLET,
            colorArgb = 0xFF147D64,
            dosageAmount = 100.0,
            dosageUnit = DosageUnit.MG,
            tabletsPerIntake = 1.0,
            packageSize = 20.0,
            remaining = 10.0,
            mealTiming = MealTiming.ANY,
            note = "",
            startDate = LocalDate.now(),
            endDate = null,
            scheduleKind = ScheduleKind.DAILY,
        ),
        scheduledAt = System.currentTimeMillis(),
        status = IntakeStatus.PENDING,
    )
}
