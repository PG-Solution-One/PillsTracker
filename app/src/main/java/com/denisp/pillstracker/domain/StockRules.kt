package com.denisp.pillstracker.domain

import com.denisp.pillstracker.model.Medicine

object StockRules {
    fun isLowStock(medicine: Medicine): Boolean =
        medicine.trackStock &&
            medicine.remaining >= 0.0 &&
            medicine.tabletsPerIntake > 0.0 &&
            medicine.remaining <= medicine.tabletsPerIntake * LOW_STOCK_INTAKES

    fun remainingAfterRefill(
        currentRemaining: Double,
        addedAmount: Double,
    ): Double? {
        if (!currentRemaining.isFinite() || currentRemaining < 0.0) return null
        if (!addedAmount.isFinite() || addedAmount <= 0.0) return null

        return (currentRemaining + addedAmount).takeIf { it.isFinite() }
    }

    private const val LOW_STOCK_INTAKES = 3
}
