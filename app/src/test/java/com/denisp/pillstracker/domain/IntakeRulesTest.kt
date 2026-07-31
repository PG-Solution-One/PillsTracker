package com.denisp.pillstracker.domain

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

    private fun timestamp(date: LocalDate): Long =
        date.atTime(9, 0).atZone(zone).toInstant().toEpochMilli()
}
