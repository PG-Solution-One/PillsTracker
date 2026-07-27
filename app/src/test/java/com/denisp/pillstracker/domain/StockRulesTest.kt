package com.denisp.pillstracker.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StockRulesTest {
    @Test
    fun `refill adds a new package to current remaining stock`() {
        assertEquals(
            30.0,
            StockRules.remainingAfterRefill(
                currentRemaining = 5.0,
                addedAmount = 25.0,
            ) ?: error("Valid refill must produce a remaining amount"),
            0.0,
        )
    }

    @Test
    fun `refill rejects non-positive and non-finite amounts`() {
        assertNull(StockRules.remainingAfterRefill(currentRemaining = 5.0, addedAmount = 0.0))
        assertNull(StockRules.remainingAfterRefill(currentRemaining = 5.0, addedAmount = -1.0))
        assertNull(StockRules.remainingAfterRefill(currentRemaining = 5.0, addedAmount = Double.NaN))
    }
}
