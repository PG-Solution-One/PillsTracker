package com.denisp.pillstracker.domain

import com.denisp.pillstracker.model.IntakeStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object IntakeRules {
    fun canChangeStatus(
        scheduledAt: Long,
        today: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): Boolean {
        val scheduledDate = Instant.ofEpochMilli(scheduledAt).atZone(zoneId).toLocalDate()
        return !scheduledDate.isAfter(today)
    }

    fun canMarkTaken(
        remaining: Double,
        tabletsPerIntake: Double,
        currentStatus: IntakeStatus,
    ): Boolean =
        currentStatus == IntakeStatus.TAKEN ||
            remaining + STOCK_EPSILON >= tabletsPerIntake

    private const val STOCK_EPSILON = 0.000_001
}
