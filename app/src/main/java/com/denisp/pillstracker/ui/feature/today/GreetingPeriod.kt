package com.denisp.pillstracker.ui.feature.today

internal enum class GreetingPeriod {
    MORNING,
    DAY,
    EVENING,
    NIGHT,
}

internal fun greetingPeriodForHour(hour: Int): GreetingPeriod {
    require(hour in 0..23) { "Hour must be between 0 and 23" }
    return when (hour) {
        in 5..11 -> GreetingPeriod.MORNING
        in 12..17 -> GreetingPeriod.DAY
        in 18..22 -> GreetingPeriod.EVENING
        else -> GreetingPeriod.NIGHT
    }
}
