package com.denisp.pillstracker.ui.feature.medicines

import com.denisp.pillstracker.model.IntakeStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class MedicineDetailsScreenTest {
    @Test
    fun `status labels distinguish completed upcoming and waiting doses`() {
        val now = 1_000L

        assertEquals(
            "Принято",
            medicineDoseStatusLabel(IntakeStatus.TAKEN, scheduledAt = 500L, nowMillis = now),
        )
        assertEquals(
            "Пропущено",
            medicineDoseStatusLabel(IntakeStatus.SKIPPED, scheduledAt = 500L, nowMillis = now),
        )
        assertEquals(
            "Предстоящее",
            medicineDoseStatusLabel(IntakeStatus.PENDING, scheduledAt = 1_500L, nowMillis = now),
        )
        assertEquals(
            "Ожидает",
            medicineDoseStatusLabel(IntakeStatus.PENDING, scheduledAt = 500L, nowMillis = now),
        )
        assertEquals(
            "Не сегодня",
            medicineDoseStatusLabel(status = null, scheduledAt = null, nowMillis = now),
        )
    }

    @Test
    fun `pending dose becomes overdue after thirty minutes`() {
        val scheduledAt = 1_000L

        assertEquals(
            "Ожидает",
            medicineDoseStatusLabel(
                IntakeStatus.PENDING,
                scheduledAt,
                scheduledAt + 30 * 60 * 1000L - 1,
            ),
        )
        assertEquals(
            "Просрочено",
            medicineDoseStatusLabel(
                IntakeStatus.PENDING,
                scheduledAt,
                scheduledAt + 30 * 60 * 1000L,
            ),
        )
    }
}
