package com.denisp.pillstracker.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

enum class MedicineForm(val title: String) {
    TABLET("Таблетка"),
    CAPSULE("Капсула"),
    POWDER("Порошок"),
    INJECTION("Инъекция"),
    OTHER("Другое"),
}

enum class PillShape(val title: String) {
    ROUND("Круглая"),
    OVAL("Овальная"),
    CAPSULE("Капсула"),
    OBLONG("Продолговатая"),
}

enum class DosageUnit(val title: String) {
    MG("мг"),
    G("г"),
    MCG("мкг"),
    ML("мл"),
    DROPS("капли"),
    IU("МЕ"),
}

enum class MealTiming(val title: String) {
    ANY("Неважно"),
    BEFORE("До еды"),
    WITH_FOOD("Во время еды"),
    AFTER("После еды"),
}

enum class ScheduleKind(val title: String) {
    DAILY("Каждый день"),
    EVERY_OTHER_DAY("Через день"),
    SELECTED_DAYS("По дням недели"),
    AS_NEEDED("По необходимости"),
}

enum class MedicineState {
    ACTIVE,
    PAUSED,
    ARCHIVED,
}

enum class IntakeStatus {
    PENDING,
    TAKEN,
    SKIPPED,
}

enum class ThemeMode(val title: String) {
    SYSTEM("Как на устройстве"),
    LIGHT("Светлая"),
    DARK("Тёмная"),
}

data class ScheduleTime(
    val id: Long = 0,
    val medicineId: Long = 0,
    val minuteOfDay: Int,
    val dayMask: Int = ALL_DAYS_MASK,
)

data class Medicine(
    val id: Long = 0,
    val name: String,
    val form: MedicineForm,
    val pillShape: PillShape = PillShape.ROUND,
    val colorArgb: Long,
    val secondaryColorArgb: Long? = null,
    val dosageAmount: Double,
    val dosageUnit: DosageUnit,
    val tabletsPerIntake: Double,
    val packageSize: Double,
    val remaining: Double,
    val mealTiming: MealTiming,
    val note: String,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val scheduleKind: ScheduleKind,
    val state: MedicineState = MedicineState.ACTIVE,
    val times: List<ScheduleTime> = emptyList(),
) {
    val dosage: String
        get() = "${dosageAmount.displayAmount()} ${dosageUnit.title}"
}

data class IntakeRecord(
    val id: Long = 0,
    val medicineId: Long,
    val scheduledAt: Long,
    val status: IntakeStatus,
    val updatedAt: Long,
)

data class ScheduledDose(
    val medicine: Medicine,
    val scheduledAt: Long,
    val status: IntakeStatus,
) {
    val date: LocalDate
        get() = Instant.ofEpochMilli(scheduledAt).atZone(ZoneId.systemDefault()).toLocalDate()
}

data class TrackerSnapshot(
    val medicines: List<Medicine> = emptyList(),
    val records: List<IntakeRecord> = emptyList(),
)

const val ALL_DAYS_MASK = 0b1111111

fun dayMask(dayValue: Int): Int = 1 shl (dayValue - 1)

fun Double.displayAmount(): String {
    val rounded = toLong()
    return if (this == rounded.toDouble()) rounded.toString() else toString().trimEnd('0').trimEnd('.')
}
