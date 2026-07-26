package com.denisp.pillstracker.ui.feature.editor

internal data class EditableScheduleTime(
    val minuteOfDay: Int,
    val dayMask: Int,
)

internal enum class CourseEndMode(val title: String) {
    WITHOUT_END("Без окончания"),
    END_DATE("До даты"),
    DAYS_COUNT("Количество дней"),
}

internal data class EditorStep(
    val title: String,
    val subtitle: String,
)

internal val editorSteps = listOf(
    EditorStep("Лекарство", "Название, форма и цвет"),
    EditorStep("Дозировка", "Сколько принимать и сколько осталось"),
    EditorStep("Курс", "Период и схема приёма"),
    EditorStep("Время", "Когда напоминать"),
    EditorStep("Инструкция", "Еда, заметка и проверка"),
)
