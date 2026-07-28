package com.denisp.pillstracker.ui.feature.editor

import org.junit.Assert.assertEquals
import org.junit.Test

class MedicineNoteLimitTest {
    @Test
    fun `short note remains unchanged`() {
        val note = "Запивать стаканом воды"

        assertEquals(note, limitMedicineNote(note))
    }

    @Test
    fun `long note is limited to one hundred characters`() {
        assertEquals(
            "а".repeat(MAX_MEDICINE_NOTE_LENGTH),
            limitMedicineNote("а".repeat(MAX_MEDICINE_NOTE_LENGTH + 20)),
        )
    }

    @Test
    fun `emoji at the limit is not split`() {
        val expected = "а".repeat(MAX_MEDICINE_NOTE_LENGTH - 1) + "💊"

        val limited = limitMedicineNote(expected + "лишнее")

        assertEquals(expected, limited)
        assertEquals(MAX_MEDICINE_NOTE_LENGTH, medicineNoteLength(limited))
    }
}
