package com.denisp.pillstracker.ui.feature.editor

internal fun medicineFormIndexForPage(page: Int, formsCount: Int): Int {
    require(formsCount > 0)
    return Math.floorMod(page, formsCount)
}

internal fun initialMedicineFormPage(selectedIndex: Int, formsCount: Int): Int {
    require(formsCount > 0)
    require(selectedIndex in 0 until formsCount)

    val middlePage = Int.MAX_VALUE / 2
    return middlePage - medicineFormIndexForPage(middlePage, formsCount) + selectedIndex
}
