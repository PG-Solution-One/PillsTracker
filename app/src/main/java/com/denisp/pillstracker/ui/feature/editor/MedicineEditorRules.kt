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

internal fun secondaryColorAfterFormChange(
    selectedForm: MedicineForm,
    currentSecondaryColor: Long?,
): Long? = when {
    selectedForm == MedicineForm.TABLET || selectedForm == MedicineForm.CAPSULE ->
        currentSecondaryColor

    else -> null
}
