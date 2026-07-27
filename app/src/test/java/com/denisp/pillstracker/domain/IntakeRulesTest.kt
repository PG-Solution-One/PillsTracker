package com.denisp.pillstracker.domain

import com.denisp.pillstracker.model.IntakeStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class IntakeRulesTest {
    private val zone = ZoneId.of("Europe/Moscow")
    private val today = LocalDate.of(2026, 7, 27)

    @Test
    fun `allows status changes for today and past dates`() {
        assertTrue(IntakeRules.canChangeStatus(timestamp(today), today, zone))
        assertTrue(IntakeRules.canChangeStatus(timestamp(today.minusDays(1)), today, zone))
    }

    @Test
    fun `rejects status changes for future dates`() {
        assertFalse(IntakeRules.canChangeStatus(timestamp(today.plusDays(1)), today, zone))
    }

    @Test
    fun `requires enough stock for one dose`() {
        assertTrue(IntakeRules.canMarkTaken(2.0, 2.0, IntakeStatus.PENDING))
        assertFalse(IntakeRules.canMarkTaken(1.0, 2.0, IntakeStatus.PENDING))
    }

    @Test
    fun `allows changing an already taken dose even when stock is empty`() {
        assertTrue(IntakeRules.canMarkTaken(0.0, 1.0, IntakeStatus.TAKEN))
    }

    private fun timestamp(date: LocalDate): Long =
        date.atTime(9, 0).atZone(zone).toInstant().toEpochMilli()
}
