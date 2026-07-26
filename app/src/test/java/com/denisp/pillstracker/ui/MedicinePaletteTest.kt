package com.denisp.pillstracker.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MedicinePaletteTest {
    @Test
    fun paletteContainsValidArgbColors() {
        MedicinePalette.forEach { color ->
            assertTrue(color in 0..0xFFFFFFFFL)
            assertEquals(0xFF, color ushr 24)
        }
    }
}
