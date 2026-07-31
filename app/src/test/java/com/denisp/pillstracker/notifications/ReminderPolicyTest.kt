package com.denisp.pillstracker.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class ReminderPolicyTest {
    private val zone = ZoneId.of("Europe/Moscow")

    @Test
    fun `uses 30 minutes then 2 hours then 4 hour intervals`() {
        val cycleStartedAt = timestamp(2026, 7, 29, 9, 0)

        assertEquals(timestamp(2026, 7, 29, 9, 30), ReminderPolicy.repeatAt(cycleStartedAt, 0))
        assertEquals(timestamp(2026, 7, 29, 11, 30), ReminderPolicy.repeatAt(cycleStartedAt, 1))
        assertEquals(timestamp(2026, 7, 29, 15, 30), ReminderPolicy.repeatAt(cycleStartedAt, 2))
        assertEquals(timestamp(2026, 7, 29, 19, 30), ReminderPolicy.repeatAt(cycleStartedAt, 3))
        assertEquals(timestamp(2026, 7, 29, 23, 30), ReminderPolicy.repeatAt(cycleStartedAt, 4))
    }

    @Test
    fun `finds first future repeat after restored state`() {
        val cycleStartedAt = timestamp(2026, 7, 29, 9, 0)

        val repeat = ReminderPolicy.nextRepeat(
            cycleStartedAt = cycleStartedAt,
            fromStage = 0,
            now = timestamp(2026, 7, 29, 12, 0),
            dayEndAt = timestamp(2026, 7, 30, 0, 0),
        )

        assertEquals(2, repeat?.stage)
        assertEquals(timestamp(2026, 7, 29, 15, 30), repeat?.triggerAt)
    }

    @Test
    fun `does not carry snooze across midnight`() {
        val cycleStartedAt = timestamp(2026, 7, 29, 23, 45)

        val repeat = ReminderPolicy.nextRepeat(
            cycleStartedAt = cycleStartedAt,
            fromStage = 0,
            now = cycleStartedAt,
            dayEndAt = timestamp(2026, 7, 30, 0, 0),
        )

        assertNull(repeat)
    }

    @Test
    fun `uses next local midnight as day end`() {
        val scheduledAt = timestamp(2026, 7, 29, 22, 0)

        assertEquals(
            timestamp(2026, 7, 30, 0, 0),
            ReminderPolicy.dayEndAt(scheduledAt, zone),
        )
    }

    private fun timestamp(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
    ): Long =
        LocalDate.of(year, month, day)
            .atTime(hour, minute)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()
}
