package com.denisp.pillstracker.data.local

import android.content.Context
import androidx.core.content.edit
import com.denisp.pillstracker.model.InterfaceMode
import com.denisp.pillstracker.model.ThemeMode

class ThemePreferences(context: Context) {
    private val preferences = context.getSharedPreferences("appearance", Context.MODE_PRIVATE)

    fun load(): ThemeMode = runCatching {
        ThemeMode.valueOf(preferences.getString(KEY_THEME, ThemeMode.SYSTEM.name).orEmpty())
    }.getOrDefault(ThemeMode.SYSTEM)

    fun save(mode: ThemeMode) {
        preferences.edit { putString(KEY_THEME, mode.name) }
    }

    fun loadInterfaceMode(): InterfaceMode = InterfaceMode.fromStoredValue(
        preferences.getString(KEY_INTERFACE_MODE, null),
    )

    fun saveInterfaceMode(mode: InterfaceMode) {
        preferences.edit { putString(KEY_INTERFACE_MODE, mode.name) }
    }

    companion object {
        private const val KEY_THEME = "theme"
        private const val KEY_INTERFACE_MODE = "interface_mode"
    }
}
