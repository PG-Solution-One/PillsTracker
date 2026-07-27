package com.denisp.pillstracker.model

import java.time.LocalDate

const val DEFAULT_MEDICINE_BACKGROUND_ARGB = 0xFFE7EAEEL

enum class MedicineForm(val title: String) {
    TABLET("Таблетка"),
    CAPSULE("Капсула"),
    POWDER("Порошок"),
    INJECTION("Инъекция"),
    DROPS("Капли"),
    SYRUP("Сироп"),
    SPRAY("Спрей"),
    OINTMENT("Мазь"),
    SUPPOSITORY("Свечи"),
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

enum class MedicineState {
    ACTIVE,
    PAUSED,
    ARCHIVED,
}

data class Medicine(
    val id: Long = 0,
    val name: String,
    val form: MedicineForm,
    val pillShape: PillShape = PillShape.ROUND,
    val colorArgb: Long,
    val secondaryColorArgb: Long? = null,
    val backgroundColorArgb: Long = DEFAULT_MEDICINE_BACKGROUND_ARGB,
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

fun Double.displayAmount(): String {
    val rounded = toLong()
    return if (this == rounded.toDouble()) rounded.toString() else toString().trimEnd('0').trimEnd('.')
}
