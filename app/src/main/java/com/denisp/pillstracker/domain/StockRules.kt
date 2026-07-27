package com.denisp.pillstracker.domain

object StockRules {
    fun remainingAfterRefill(
        currentRemaining: Double,
        addedAmount: Double,
    ): Double? {
        if (!currentRemaining.isFinite() || currentRemaining < 0.0) return null
        if (!addedAmount.isFinite() || addedAmount <= 0.0) return null

        return (currentRemaining + addedAmount).takeIf { it.isFinite() }
    }
}
