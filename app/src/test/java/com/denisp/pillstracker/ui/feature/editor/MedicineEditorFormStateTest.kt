package com.denisp.pillstracker.ui.feature.editor

import com.denisp.pillstracker.model.ALL_DAYS_MASK
import com.denisp.pillstracker.model.DosageUnit
import com.denisp.pillstracker.model.MealTiming
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.model.MedicineForm
import com.denisp.pillstracker.model.MedicineState
import com.denisp.pillstracker.model.PillShape
import com.denisp.pillstracker.model.ScheduleKind
import com.denisp.pillstracker.model.ScheduleTime
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MedicineEditorFormStateTest {
    @Test
    fun `capsule remains single colored until second color is enabled manually`() {
        assertEquals(
            null,
            secondaryColorAfterFormChange(
                selectedForm = MedicineForm.CAPSULE,
                currentSecondaryColor = null,
            ),
        )
    }

    @Test
    fun `manual second color is preserved between capsule and tablet`() {
        assertEquals(
            0xFF556677,
            secondaryColorAfterFormChange(
                selectedForm = MedicineForm.TABLET,
                currentSecondaryColor = 0xFF556677,
            ),
        )
    }

    @Test
    fun `unsupported form clears second color`() {
        assertEquals(
            null,
            secondaryColorAfterFormChange(
                selectedForm = MedicineForm.DROPS,
                currentSecondaryColor = 0xFF556677,
            ),
        )
    }

    @Test
    fun `existing medicine survives editor draft round trip`() {
        val initial = medicine()

        val restored = MedicineEditorDraft.from(initial).toMedicine(initial)

        assertEquals(initial.id, restored.id)
        assertEquals(initial.name, restored.name)
        assertEquals(initial.form, restored.form)
        assertEquals(initial.pillShape, restored.pillShape)
        assertEquals(initial.colorArgb, restored.colorArgb)
        assertEquals(initial.secondaryColorArgb, restored.secondaryColorArgb)
        assertEquals(initial.backgroundColorArgb, restored.backgroundColorArgb)
        assertEquals(initial.dosageAmount, restored.dosageAmount, 0.0)
        assertEquals(initial.dosageUnit, restored.dosageUnit)
        assertEquals(initial.tabletsPerIntake, restored.tabletsPerIntake, 0.0)
        assertEquals(initial.packageSize, restored.packageSize, 0.0)
        assertEquals(initial.remaining, restored.remaining, 0.0)
        assertEquals(initial.trackStock, restored.trackStock)
        assertEquals(initial.mealTiming, restored.mealTiming)
        assertEquals(initial.note, restored.note)
        assertEquals(initial.startDate, restored.startDate)
        assertEquals(initial.endDate, restored.endDate)
        assertEquals(initial.scheduleKind, restored.scheduleKind)
        assertEquals(initial.state, restored.state)
        assertEquals(
            initial.times,
            restored.times,
        )
    }

    @Test
    fun `validation points to first section containing an error`() {
        val valid = MedicineEditorDraft.from(medicine())

        assertNull(valid.firstInvalidStep())
        assertEquals(0, valid.copy(name = " ").firstInvalidStep())
        assertEquals(1, valid.copy(dosageAmount = "0").firstInvalidStep())
        assertEquals(
            2,
            valid.copy(
                courseEndMode = CourseEndMode.END_DATE,
                endEpochDay = valid.startEpochDay - 1,
            ).firstInvalidStep(),
        )
        assertEquals(
            3,
            valid.copy(
                scheduleKind = ScheduleKind.SELECTED_DAYS,
                times = listOf(EditableScheduleTime(8 * 60, 0)),
            ).firstInvalidStep(),
        )
    }

    @Test
    fun `as needed schedule is saved without reminder times`() {
        val initial = medicine()
        val draft = MedicineEditorDraft.from(initial).copy(scheduleKind = ScheduleKind.AS_NEEDED)

        assertEquals(emptyList<ScheduleTime>(), draft.toMedicine(initial).times)
    }

    @Test
    fun `overlapping times are rejected`() {
        val draft = MedicineEditorDraft.from(medicine()).copy(
            scheduleKind = ScheduleKind.SELECTED_DAYS,
            times = listOf(
                EditableScheduleTime(8 * 60, 0b0000011),
                EditableScheduleTime(8 * 60, 0b0000010),
            ),
        )

        assertEquals(3, draft.firstInvalidStep())
    }

    @Test
    fun `same time on disjoint selected days is allowed`() {
        val draft = MedicineEditorDraft.from(medicine()).copy(
            scheduleKind = ScheduleKind.SELECTED_DAYS,
            times = listOf(
                EditableScheduleTime(8 * 60, 0b0000001),
                EditableScheduleTime(8 * 60, 0b0000010),
            ),
        )

        assertNull(draft.firstInvalidStep())
    }

    private fun medicine() = Medicine(
        id = 42,
        name = "Витамин D",
        form = MedicineForm.CAPSULE,
        pillShape = PillShape.OVAL,
        colorArgb = 0xFF112233,
        secondaryColorArgb = 0xFF445566,
        backgroundColorArgb = 0xFFCCDDEE,
        dosageAmount = 1000.0,
        dosageUnit = DosageUnit.IU,
        tabletsPerIntake = 1.0,
        packageSize = 60.0,
        remaining = 25.0,
        trackStock = true,
        mealTiming = MealTiming.WITH_FOOD,
        note = "После завтрака",
        startDate = LocalDate.of(2026, 7, 1),
        endDate = LocalDate.of(2026, 8, 1),
        scheduleKind = ScheduleKind.SELECTED_DAYS,
        state = MedicineState.PAUSED,
        times = listOf(
            ScheduleTime(
                id = 7,
                medicineId = 42,
                minuteOfDay = 8 * 60,
                dayMask = ALL_DAYS_MASK xor 1,
                effectiveFromMillis = 123_456_789L,
            ),
        ),
    )
}
