package com.denisp.pillstracker.notifications

internal enum class AlarmPurpose(
    val requiresExactTiming: Boolean,
) {
    SCHEDULED_DOSE(requiresExactTiming = true),
    USER_SNOOZE(requiresExactTiming = true),
    AUTOMATIC_REPEAT(requiresExactTiming = false),
    DAY_END(requiresExactTiming = false),
}
