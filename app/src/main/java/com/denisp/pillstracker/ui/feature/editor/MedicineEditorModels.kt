package com.denisp.pillstracker.ui.feature.editor

internal data class EditableScheduleTime(
    val minuteOfDay: Int,
    val dayMask: Int,
    val id: Long = 0,
    val effectiveFromMillis: Long = 0,
)

internal fun hasOverlappingScheduleTimes(
    scheduleKind: com.denisp.pillstracker.model.ScheduleKind,
    times: List<EditableScheduleTime>,
): Boolean = times.indices.any { firstIndex ->
    ((firstIndex + 1) until times.size).any { secondIndex ->
        val first = times[firstIndex]
        val second = times[secondIndex]
        first.minuteOfDay == second.minuteOfDay &&
            (scheduleKind != com.denisp.pillstracker.model.ScheduleKind.SELECTED_DAYS ||
                first.dayMask and second.dayMask != 0)
    }
}

internal enum class CourseEndMode(val title: String) {
    WITHOUT_END("Без окончания"),
    END_DATE("До даты"),
    DAYS_COUNT("Количество дней"),
}

internal data class EditorStep(
    val title: String,
    val subtitle: String,
)

internal enum class MedicineEditSection(
    val stepIndex: Int,
    val title: String,
    val subtitle: String,
) {
    APPEARANCE(0, "Название и внешний вид", "Название, форма и оформление"),
    DOSAGE(1, "Дозировка и остаток", "Количество за приём и запас"),
    COURSE(2, "Курс", "Период и схема приёма"),
    SCHEDULE(3, "Расписание", "Время и дни приёма"),
    INSTRUCTIONS(4, "Инструкция", "Связь с едой и заметка"),
    ;

    companion object {
        fun fromStep(step: Int): MedicineEditSection = entries.first { it.stepIndex == step }
    }
}

internal val editorSteps = listOf(
    EditorStep("Лекарство", "Название, форма и цвет"),
    EditorStep("Дозировка", "Сколько принимать и сколько осталось"),
    EditorStep("Курс", "Период и схема приёма"),
    EditorStep("Время", "Когда напоминать"),
    EditorStep("Инструкция", "Еда, заметка и проверка"),
)
