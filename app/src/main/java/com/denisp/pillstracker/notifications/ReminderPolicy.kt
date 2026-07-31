package com.denisp.pillstracker.notifications

import com.denisp.pillstracker.domain.DoseTimingPolicy
import java.time.Instant
import java.time.ZoneId

internal object ReminderPolicy {
    const val FIRST_REPEAT_DELAY_MILLIS = DoseTimingPolicy.OVERDUE_AFTER_MILLIS
    const val SECOND_REPEAT_DELAY_MILLIS = 2 * 60 * 60 * 1000L
    const val LATER_REPEAT_DELAY_MILLIS = 4 * 60 * 60 * 1000L

    fun repeatAt(cycleStartedAt: Long, stage: Int): Long {
        require(stage >= 0)
        return cycleStartedAt +
            FIRST_REPEAT_DELAY_MILLIS +
            if (stage == 0) {
                0L
            } else {
                SECOND_REPEAT_DELAY_MILLIS +
                    (stage - 1L) * LATER_REPEAT_DELAY_MILLIS
            }
    }

    fun nextRepeat(
        cycleStartedAt: Long,
        fromStage: Int,
        now: Long,
        dayEndAt: Long,
    ): ReminderRepeat? {
        var stage = fromStage.coerceAtLeast(0)
        while (true) {
            val triggerAt = repeatAt(cycleStartedAt, stage)
            if (triggerAt >= dayEndAt) return null
            if (triggerAt > now) return ReminderRepeat(stage, triggerAt)
            stage += 1
        }
    }

    fun dayEndAt(
        scheduledAt: Long,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): Long =
        Instant.ofEpochMilli(scheduledAt)
            .atZone(zoneId)
            .toLocalDate()
            .plusDays(1)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()
}

internal data class ReminderRepeat(
    val stage: Int,
    val triggerAt: Long,
)
