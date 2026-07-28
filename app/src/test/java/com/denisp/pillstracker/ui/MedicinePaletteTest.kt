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

    @Test
    fun `medicine colors follow popularity order`() {
        assertEquals(
            listOf(
                0xFFF5F5F5,
                0xFFF2D45C,
                0xFFEFA0B7,
                0xFFE9A15B,
                0xFF73A7D8,
                0xFF9A725C,
                0xFF73A979,
                0xFFD96868,
                0xFF987BB8,
                0xFFA8ADB3,
                0xFF34363A,
                0xFF65B8B0,
            ),
            MedicinePalette,
        )
    }
}
