package com.denisp.pillstracker.model

import org.junit.Assert.assertEquals
import org.junit.Test

class InterfaceModeTest {
    @Test
    fun `missing stored value keeps standard interface`() {
        assertEquals(InterfaceMode.STANDARD, InterfaceMode.fromStoredValue(null))
    }

    @Test
    fun `simplified stored value is restored`() {
        assertEquals(
            InterfaceMode.SIMPLIFIED,
            InterfaceMode.fromStoredValue(InterfaceMode.SIMPLIFIED.name),
        )
    }

    @Test
    fun `unknown stored value safely falls back to standard`() {
        assertEquals(InterfaceMode.STANDARD, InterfaceMode.fromStoredValue("UNKNOWN"))
    }
}
