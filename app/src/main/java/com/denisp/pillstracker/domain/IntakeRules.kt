package com.denisp.pillstracker.domain

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
}
