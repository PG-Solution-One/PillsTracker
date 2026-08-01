package com.denisp.pillstracker.model

enum class InterfaceMode {
    STANDARD,
    SIMPLIFIED,
    ;

    companion object {
        fun fromStoredValue(value: String?): InterfaceMode =
            entries.firstOrNull { it.name == value } ?: STANDARD
    }
}
