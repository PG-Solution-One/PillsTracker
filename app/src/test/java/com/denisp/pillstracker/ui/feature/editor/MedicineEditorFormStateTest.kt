package com.denisp.pillstracker.ui.feature.editor

import com.denisp.pillstracker.model.MedicineForm
import org.junit.Assert.assertEquals
import org.junit.Test

class MedicineEditorFormStateTest {
    @Test
    fun `capsule automatically gets a second color`() {
        assertEquals(
            SecondaryColorTransition(
                color = 0xFF556677,
                automaticallyEnabledForCapsule = true,
            ),
            secondaryColorAfterFormChange(
                previousForm = MedicineForm.TABLET,
                selectedForm = MedicineForm.CAPSULE,
                currentSecondaryColor = null,
                wasAutomaticallyEnabledForCapsule = false,
                defaultSecondaryColor = 0xFF556677,
            ),
        )
    }

    @Test
    fun `automatic capsule color is cleared when returning to tablet`() {
        assertEquals(
            SecondaryColorTransition(
                color = null,
                automaticallyEnabledForCapsule = false,
            ),
            secondaryColorAfterFormChange(
                previousForm = MedicineForm.CAPSULE,
                selectedForm = MedicineForm.TABLET,
                currentSecondaryColor = 0xFF556677,
                wasAutomaticallyEnabledForCapsule = true,
                defaultSecondaryColor = 0xFF112233,
            ),
        )
    }

    @Test
    fun `intentional two color tablet remains two colored`() {
        assertEquals(
            SecondaryColorTransition(
                color = 0xFF556677,
                automaticallyEnabledForCapsule = false,
            ),
            secondaryColorAfterFormChange(
                previousForm = MedicineForm.CAPSULE,
                selectedForm = MedicineForm.TABLET,
                currentSecondaryColor = 0xFF556677,
                wasAutomaticallyEnabledForCapsule = false,
                defaultSecondaryColor = 0xFF112233,
            ),
        )
    }
}
