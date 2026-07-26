package com.denisp.pillstracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.denisp.pillstracker.model.ThemeMode

private val LightColors = lightColorScheme(
    primary = Color(0xFF147D64),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB5F2DC),
    onPrimaryContainer = Color(0xFF002117),
    secondary = Color(0xFF4D635A),
    surface = Color(0xFFF8FAF7),
    surfaceContainer = Color(0xFFEEF2EE),
    error = Color(0xFFBA1A1A),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF99D6C0),
    onPrimary = Color(0xFF00382A),
    primaryContainer = Color(0xFF00513E),
    onPrimaryContainer = Color(0xFFB5F2DC),
    secondary = Color(0xFFB4CCC1),
    surface = Color(0xFF101412),
    surfaceContainer = Color(0xFF1C211E),
    error = Color(0xFFFFB4AB),
)

@Composable
fun PillsTrackerTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val context = LocalContext.current
    val colors = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (darkTheme) DarkColors else LightColors
    }
    MaterialTheme(
        colorScheme = colors,
        content = content,
    )
}
