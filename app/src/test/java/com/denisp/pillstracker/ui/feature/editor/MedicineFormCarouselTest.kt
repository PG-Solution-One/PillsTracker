package com.denisp.pillstracker.ui.feature.editor

import org.junit.Assert.assertEquals
import org.junit.Test

class MedicineFormCarouselTest {
    @Test
    fun `initial page points to selected form near virtual center`() {
        val formsCount = 10
        val selectedIndex = 6

        val page = initialMedicineFormPage(selectedIndex, formsCount)

        assertEquals(selectedIndex, medicineFormIndexForPage(page, formsCount))
        assertEquals(selectedIndex, medicineFormIndexForPage(page + formsCount, formsCount))
    }

    @Test
    fun `page after last form wraps to first form`() {
        val formsCount = 10
        val lastFormPage = initialMedicineFormPage(formsCount - 1, formsCount)

        assertEquals(0, medicineFormIndexForPage(lastFormPage + 1, formsCount))
    }

    @Test
    fun `page before first form wraps to last form`() {
        val formsCount = 10
        val firstFormPage = initialMedicineFormPage(0, formsCount)

        assertEquals(formsCount - 1, medicineFormIndexForPage(firstFormPage - 1, formsCount))
    }
}
