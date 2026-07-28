package com.denisp.pillstracker.ui.feature.editor

import com.denisp.pillstracker.model.MedicineForm

internal const val MAX_MEDICINE_NOTE_LENGTH = 100

internal fun limitMedicineNote(note: String): String {
    val length = medicineNoteLength(note)
    if (length <= MAX_MEDICINE_NOTE_LENGTH) return note

    val endIndex = note.offsetByCodePoints(0, MAX_MEDICINE_NOTE_LENGTH)
    return note.substring(0, endIndex)
}

internal fun medicineNoteLength(note: String): Int = note.codePointCount(0, note.length)

internal fun medicineFormIndexForPage(page: Int, formsCount: Int): Int {
    require(formsCount > 0)
    return Math.floorMod(page, formsCount)
}

internal fun initialMedicineFormPage(selectedIndex: Int, formsCount: Int): Int {
    require(formsCount > 0)
    require(selectedIndex in 0 until formsCount)

    val middlePage = Int.MAX_VALUE / 2
    return middlePage - medicineFormIndexForPage(middlePage, formsCount) + selectedIndex
}

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

internal fun secondaryColorAfterFormChange(
    selectedForm: MedicineForm,
    currentSecondaryColor: Long?,
): Long? = when {
    selectedForm == MedicineForm.TABLET || selectedForm == MedicineForm.CAPSULE ->
        currentSecondaryColor

    else -> null
}

internal val editorSteps = listOf(
    EditorStep("Лекарство", "Название, форма и цвет"),
    EditorStep("Дозировка", "Сколько принимать и сколько осталось"),
    EditorStep("Курс", "Период и схема приёма"),
    EditorStep("Время", "Когда напоминать"),
    EditorStep("Инструкция", "Еда, заметка и проверка"),
)
