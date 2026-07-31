package com.denisp.pillstracker.ui.feature.editor

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.denisp.pillstracker.model.ALL_DAYS_MASK
import com.denisp.pillstracker.model.DosageUnit
import com.denisp.pillstracker.model.MealTiming
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.model.MedicineForm
import com.denisp.pillstracker.model.ScheduleKind
import com.denisp.pillstracker.model.ScheduleTime
import com.denisp.pillstracker.model.ThemeMode
import com.denisp.pillstracker.ui.theme.PillsTrackerTheme
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MedicineEditorScreenUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun editModeOpensOverviewAndSavesSelectedSection() {
        val savedMedicine = AtomicReference<Medicine?>()
        composeRule.setContent {
            PillsTrackerTheme(ThemeMode.LIGHT) {
                MedicineEditorScreen(
                    initialMedicine = medicine(),
                    onBack = {},
                    onSave = savedMedicine::set,
                )
            }
        }

        composeRule.onNodeWithText("Выберите, что хотите изменить").assertIsDisplayed()
        composeRule
            .onNodeWithContentDescription("Открыть раздел Дозировка и остаток")
            .performClick()
        composeRule.onNodeWithText("Таблеток за один приём").assertIsDisplayed()
        composeRule.onNodeWithText("1000").performTextReplacement("2000")
        composeRule.onNodeWithContentDescription("К разделам").performClick()
        composeRule.onNodeWithText("2000 МЕ", substring = true).assertIsDisplayed()

        composeRule
            .onNodeWithContentDescription("Открыть раздел Дозировка и остаток")
            .performClick()
        composeRule.onNodeWithText("Сохранить изменения").performClick()

        composeRule.runOnIdle {
            assertEquals(2000.0, savedMedicine.get()?.dosageAmount ?: 0.0, 0.0)
        }
    }

    @Test
    fun closingChangedMedicineRequiresConfirmation() {
        composeRule.setContent {
            PillsTrackerTheme(ThemeMode.LIGHT) {
                MedicineEditorScreen(
                    initialMedicine = medicine(),
                    onBack = {},
                    onSave = {},
                )
            }
        }

        composeRule
            .onNodeWithContentDescription("Открыть раздел Дозировка и остаток")
            .performClick()
        composeRule.onNodeWithText("1000").performTextReplacement("2000")
        composeRule.onNodeWithContentDescription("К разделам").performClick()
        composeRule.onNodeWithText("Закрыть").performClick()

        composeRule.onNodeWithText("Отменить изменения?").assertIsDisplayed()
        composeRule.onNodeWithText("Продолжить редактирование").assertIsDisplayed()
    }

    private fun medicine() = Medicine(
        id = 42,
        name = "Витамин D",
        form = MedicineForm.TABLET,
        colorArgb = 0xFFFFB43B,
        backgroundColorArgb = 0xFFC9E3F7,
        dosageAmount = 1000.0,
        dosageUnit = DosageUnit.IU,
        tabletsPerIntake = 1.0,
        packageSize = 60.0,
        remaining = 25.0,
        trackStock = true,
        mealTiming = MealTiming.WITH_FOOD,
        note = "После завтрака",
        startDate = LocalDate.of(2026, 7, 1),
        endDate = null,
        scheduleKind = ScheduleKind.DAILY,
        times = listOf(
            ScheduleTime(
                medicineId = 42,
                minuteOfDay = 8 * 60,
                dayMask = ALL_DAYS_MASK,
            ),
        ),
    )
}
