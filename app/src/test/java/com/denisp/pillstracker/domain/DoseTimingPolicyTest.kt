package com.denisp.pillstracker.domain

import com.denisp.pillstracker.model.DosageUnit
import com.denisp.pillstracker.model.IntakeStatus
import com.denisp.pillstracker.model.MealTiming
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.model.MedicineForm
import com.denisp.pillstracker.model.ScheduleKind
import com.denisp.pillstracker.model.ScheduledDose
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class DoseTimingPolicyTest {
    private val scheduledAt = 10_000_000L

    @Test
    fun `pending dose becomes overdue exactly thirty minutes after scheduled time`() {
        val dose = dose(IntakeStatus.PENDING)

        assertEquals(DoseTimingState.UPCOMING, DoseTimingPolicy.state(dose, scheduledAt - 1))
        assertEquals(DoseTimingState.DUE, DoseTimingPolicy.state(dose, scheduledAt))
        assertEquals(
            DoseTimingState.DUE,
            DoseTimingPolicy.state(
                dose,
                scheduledAt + DoseTimingPolicy.OVERDUE_AFTER_MILLIS - 1,
            ),
        )
        assertEquals(
            DoseTimingState.OVERDUE,
            DoseTimingPolicy.state(
                dose,
                scheduledAt + DoseTimingPolicy.OVERDUE_AFTER_MILLIS,
            ),
        )
    }

    @Test
    fun `resolved dose never becomes overdue`() {
        assertEquals(
            DoseTimingState.RESOLVED,
            DoseTimingPolicy.state(dose(IntakeStatus.TAKEN), Long.MAX_VALUE),
        )
        assertEquals(
            DoseTimingState.RESOLVED,
            DoseTimingPolicy.state(dose(IntakeStatus.SKIPPED), Long.MAX_VALUE),
        )
    }

    private fun dose(status: IntakeStatus) = ScheduledDose(
        medicine = Medicine(
            id = 1,
            name = "Лекарство",
            form = MedicineForm.TABLET,
            colorArgb = 0xFF00AA00,
            dosageAmount = 1.0,
            dosageUnit = DosageUnit.MG,
            tabletsPerIntake = 1.0,
            packageSize = 1.0,
            remaining = 1.0,
            mealTiming = MealTiming.ANY,
            note = "",
            startDate = LocalDate.of(2026, 1, 1),
            endDate = null,
            scheduleKind = ScheduleKind.DAILY,
        ),
        scheduledAt = scheduledAt,
        status = status,
    )
}
