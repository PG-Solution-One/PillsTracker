package com.denisp.pillstracker.domain

import com.denisp.pillstracker.model.IntakeStatus
import com.denisp.pillstracker.model.ScheduledDose

internal object DoseTimingPolicy {
    const val OVERDUE_AFTER_MILLIS = 30 * 60 * 1000L

    fun state(
        dose: ScheduledDose,
        nowMillis: Long,
    ): DoseTimingState = when {
        dose.status != IntakeStatus.PENDING -> DoseTimingState.RESOLVED
        nowMillis < dose.scheduledAt -> DoseTimingState.UPCOMING
        nowMillis < dose.scheduledAt + OVERDUE_AFTER_MILLIS -> DoseTimingState.DUE
        else -> DoseTimingState.OVERDUE
    }

    fun isOverdue(dose: ScheduledDose, nowMillis: Long): Boolean =
        state(dose, nowMillis) == DoseTimingState.OVERDUE
}

internal enum class DoseTimingState {
    UPCOMING,
    DUE,
    OVERDUE,
    RESOLVED,
}
