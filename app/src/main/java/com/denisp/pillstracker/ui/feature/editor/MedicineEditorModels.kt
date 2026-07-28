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

internal data class SecondaryColorTransition(
    val color: Long?,
    val automaticallyEnabledForCapsule: Boolean,
)

internal fun secondaryColorAfterFormChange(
    previousForm: MedicineForm,
    selectedForm: MedicineForm,
    currentSecondaryColor: Long?,
    wasAutomaticallyEnabledForCapsule: Boolean,
    defaultSecondaryColor: Long,
): SecondaryColorTransition = when {
    selectedForm == MedicineForm.CAPSULE && currentSecondaryColor == null ->
        SecondaryColorTransition(defaultSecondaryColor, automaticallyEnabledForCapsule = true)

    selectedForm == MedicineForm.TABLET &&
        previousForm == MedicineForm.CAPSULE &&
        wasAutomaticallyEnabledForCapsule ->
        SecondaryColorTransition(color = null, automaticallyEnabledForCapsule = false)

    selectedForm == MedicineForm.TABLET || selectedForm == MedicineForm.CAPSULE ->
        SecondaryColorTransition(currentSecondaryColor, automaticallyEnabledForCapsule = false)

    else -> SecondaryColorTransition(color = null, automaticallyEnabledForCapsule = false)
}

internal val editorSteps = listOf(
    EditorStep("Лекарство", "Название, форма и цвет"),
    EditorStep("Дозировка", "Сколько принимать и сколько осталось"),
    EditorStep("Курс", "Период и схема приёма"),
    EditorStep("Время", "Когда напоминать"),
    EditorStep("Инструкция", "Еда, заметка и проверка"),
)
