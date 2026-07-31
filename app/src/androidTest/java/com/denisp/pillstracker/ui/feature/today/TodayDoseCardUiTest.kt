package com.denisp.pillstracker.ui.feature.today

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
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
class TodayDoseCardUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun longPressOpensQuickActionsInsteadOfMedicineDetails() {
        val openCount = AtomicInteger()
        val longPressCount = AtomicInteger()
        val dose = ScheduledDose(
            medicine = medicine(),
            scheduledAt = System.currentTimeMillis(),
            status = IntakeStatus.PENDING,
        )

        composeRule.setContent {
            MaterialTheme {
                TodayDoseCard(
                    dose = dose,
                    isNext = true,
                    onStatus = {},
                    onOpen = { openCount.incrementAndGet() },
                    onLongPress = { longPressCount.incrementAndGet() },
                    nowMillis = System.currentTimeMillis(),
                )
            }
        }

        composeRule.onNodeWithText("Аспирин").performTouchInput { longClick() }

        assertEquals(0, openCount.get())
        assertEquals(1, longPressCount.get())
    }

    private fun medicine() = Medicine(
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
    )
}
