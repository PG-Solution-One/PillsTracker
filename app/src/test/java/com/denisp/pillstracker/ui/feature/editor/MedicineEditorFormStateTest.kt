package com.denisp.pillstracker.ui.feature.editor

import com.denisp.pillstracker.model.MedicineForm
import org.junit.Assert.assertEquals
import org.junit.Test

class MedicineEditorFormStateTest {
    @Test
    fun `capsule remains single colored until second color is enabled manually`() {
        assertEquals(
            null,
            secondaryColorAfterFormChange(
                selectedForm = MedicineForm.CAPSULE,
                currentSecondaryColor = null,
            ),
        )
    }

    @Test
    fun `manual second color is preserved between capsule and tablet`() {
        assertEquals(
            0xFF556677,
            secondaryColorAfterFormChange(
                selectedForm = MedicineForm.TABLET,
                currentSecondaryColor = 0xFF556677,
            ),
        )
    }

    @Test
    fun `unsupported form clears second color`() {
        assertEquals(
            null,
            secondaryColorAfterFormChange(
                selectedForm = MedicineForm.DROPS,
                currentSecondaryColor = 0xFF556677,
            ),
        )
    }
}
