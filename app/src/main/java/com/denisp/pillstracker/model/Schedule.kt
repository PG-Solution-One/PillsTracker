package com.denisp.pillstracker.model

enum class ScheduleKind(val title: String) {
    DAILY("Каждый день"),
    EVERY_OTHER_DAY("Через день"),
    SELECTED_DAYS("По дням недели"),
    AS_NEEDED("По необходимости"),
}

data class ScheduleTime(
    val id: Long = 0,
    val medicineId: Long = 0,
    val minuteOfDay: Int,
    val dayMask: Int = ALL_DAYS_MASK,
    val effectiveFromMillis: Long = 0,
)

const val ALL_DAYS_MASK = 0b1111111

fun dayMask(dayValue: Int): Int = 1 shl (dayValue - 1)
