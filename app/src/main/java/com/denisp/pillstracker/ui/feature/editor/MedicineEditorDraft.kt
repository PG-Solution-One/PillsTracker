package com.denisp.pillstracker.ui.feature.editor

import com.denisp.pillstracker.model.ALL_DAYS_MASK
import com.denisp.pillstracker.model.DEFAULT_MEDICINE_BACKGROUND_ARGB
import com.denisp.pillstracker.model.DosageUnit
import com.denisp.pillstracker.model.MealTiming
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.model.MedicineForm
import com.denisp.pillstracker.model.MedicineState
import com.denisp.pillstracker.model.PillShape
import com.denisp.pillstracker.model.ScheduleKind
import com.denisp.pillstracker.model.ScheduleTime
import com.denisp.pillstracker.model.displayAmount
import com.denisp.pillstracker.ui.MedicinePalette
import java.time.LocalDate
import java.time.temporal.ChronoUnit

internal data class MedicineEditorDraft(
    val name: String,
    val form: MedicineForm,
    val pillShape: PillShape,
    val colorArgb: Long,
    val secondaryColorArgb: Long?,
    val backgroundColorArgb: Long,
    val dosageAmount: String,
    val dosageUnit: DosageUnit,
    val tabletsPerIntake: String,
    val packageSize: String,
    val remaining: String,
    val trackStock: Boolean,
    val startEpochDay: Long,
    val endEpochDay: Long,
    val courseEndMode: CourseEndMode,
    val courseDays: String,
    val scheduleKind: ScheduleKind,
    val times: List<EditableScheduleTime>,
    val mealTiming: MealTiming,
    val note: String,
) {
    val dosage: Double?
        get() = dosageAmount.toDecimalOrNull()

    val tablets: Double?
        get() = tabletsPerIntake.toDecimalOrNull()

    val pack: Double?
        get() = packageSize.toDecimalOrNull()

    val stock: Double?
        get() = remaining.toDecimalOrNull()

    val days: Long?
        get() = courseDays.toLongOrNull()

    val startDate: LocalDate
        get() = LocalDate.ofEpochDay(startEpochDay)

    val endDate: LocalDate?
        get() = when (courseEndMode) {
            CourseEndMode.WITHOUT_END -> null
            CourseEndMode.END_DATE -> LocalDate.ofEpochDay(endEpochDay)
            CourseEndMode.DAYS_COUNT -> days
                ?.takeIf { it > 0 }
                ?.let { startDate.plusDays(it - 1) }
        }

    fun isStepValid(step: Int): Boolean = when (step) {
        0 -> name.isNotBlank()
        1 -> dosage != null && dosage!! > 0 && tablets != null && tablets!! > 0 &&
            (!trackStock || pack != null && pack!! > 0 && stock != null && stock!! >= 0)
        2 -> endDate?.isBefore(startDate) != true &&
            (courseEndMode != CourseEndMode.DAYS_COUNT || days != null && days!! > 0)
        3 -> scheduleKind == ScheduleKind.AS_NEEDED ||
            times.isNotEmpty() && times.all {
                scheduleKind != ScheduleKind.SELECTED_DAYS || it.dayMask != 0
            } && !hasOverlappingScheduleTimes(scheduleKind, times)
        else -> true
    }

    fun firstInvalidStep(): Int? = editorSteps.indices.firstOrNull { !isStepValid(it) }

    fun toMedicine(initialMedicine: Medicine?): Medicine = Medicine(
        id = initialMedicine?.id ?: 0,
        name = name.trim(),
        form = form,
        pillShape = pillShape,
        colorArgb = colorArgb,
        secondaryColorArgb = secondaryColorArgb,
        backgroundColorArgb = backgroundColorArgb,
        dosageAmount = dosage ?: 0.0,
        dosageUnit = dosageUnit,
        tabletsPerIntake = tablets ?: 1.0,
        packageSize = pack ?: initialMedicine?.packageSize ?: 0.0,
        remaining = stock ?: initialMedicine?.remaining ?: 0.0,
        trackStock = trackStock,
        mealTiming = mealTiming,
        note = limitMedicineNote(note.trim()),
        startDate = startDate,
        endDate = endDate,
        scheduleKind = scheduleKind,
        state = initialMedicine?.state ?: MedicineState.ACTIVE,
        times = if (scheduleKind == ScheduleKind.AS_NEEDED) {
            emptyList()
        } else {
            times.map {
                ScheduleTime(
                    id = it.id,
                    medicineId = initialMedicine?.id ?: 0,
                    minuteOfDay = it.minuteOfDay,
                    dayMask = if (scheduleKind == ScheduleKind.SELECTED_DAYS) {
                        it.dayMask
                    } else {
                        ALL_DAYS_MASK
                    },
                    effectiveFromMillis = it.effectiveFromMillis,
                )
            }
        },
    )

    companion object {
        fun from(
            medicine: Medicine?,
            today: LocalDate = LocalDate.now(),
        ): MedicineEditorDraft {
            val startDate = medicine?.startDate ?: today
            val endDate = medicine?.endDate ?: today.plusDays(6)
            val sourceTimes = medicine?.times.orEmpty()
            return MedicineEditorDraft(
                name = medicine?.name.orEmpty(),
                form = medicine?.form ?: MedicineForm.TABLET,
                pillShape = medicine?.pillShape ?: PillShape.ROUND,
                colorArgb = medicine?.colorArgb ?: MedicinePalette.first(),
                secondaryColorArgb = medicine?.secondaryColorArgb,
                backgroundColorArgb = medicine?.backgroundColorArgb
                    ?: DEFAULT_MEDICINE_BACKGROUND_ARGB,
                dosageAmount = medicine?.dosageAmount?.displayAmount().orEmpty(),
                dosageUnit = medicine?.dosageUnit ?: DosageUnit.MG,
                tabletsPerIntake = medicine?.tabletsPerIntake?.displayAmount() ?: "1",
                packageSize = medicine?.packageSize?.displayAmount().orEmpty(),
                remaining = medicine?.remaining?.displayAmount().orEmpty(),
                trackStock = medicine?.trackStock ?: false,
                startEpochDay = startDate.toEpochDay(),
                endEpochDay = endDate.toEpochDay(),
                courseEndMode = if (medicine?.endDate == null) {
                    CourseEndMode.WITHOUT_END
                } else {
                    CourseEndMode.END_DATE
                },
                courseDays = medicine?.endDate?.let {
                    (ChronoUnit.DAYS.between(medicine.startDate, it) + 1).toString()
                } ?: "7",
                scheduleKind = medicine?.scheduleKind ?: ScheduleKind.DAILY,
                times = if (sourceTimes.isEmpty()) {
                    listOf(EditableScheduleTime(8 * 60, ALL_DAYS_MASK))
                } else {
                    sourceTimes.map {
                        EditableScheduleTime(
                            minuteOfDay = it.minuteOfDay,
                            dayMask = it.dayMask,
                            id = it.id,
                            effectiveFromMillis = it.effectiveFromMillis,
                        )
                    }
                },
                mealTiming = medicine?.mealTiming ?: MealTiming.ANY,
                note = limitMedicineNote(medicine?.note.orEmpty()),
            )
        }
    }
}

private fun String.toDecimalOrNull(): Double? = replace(',', '.').toDoubleOrNull()
