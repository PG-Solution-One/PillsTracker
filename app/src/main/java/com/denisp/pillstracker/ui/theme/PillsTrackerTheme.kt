package com.denisp.pillstracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.denisp.pillstracker.model.ThemeMode

private val LightColors = lightColorScheme(
    primary = Color(0xFF006C51),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA7F2D4),
    onPrimaryContainer = Color(0xFF002117),
    secondary = Color(0xFF48645A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCBE9DD),
    onSecondaryContainer = Color(0xFF052019),
    tertiary = Color(0xFF3F6375),
    onTertiary = Color.White,
    background = Color(0xFFF6FBF8),
    onBackground = Color(0xFF171D1A),
    surface = Color(0xFFF6FBF8),
    onSurface = Color(0xFF171D1A),
    surfaceVariant = Color(0xFFDCE5E0),
    onSurfaceVariant = Color(0xFF404944),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF0F5F2),
    surfaceContainer = Color(0xFFE7EEE9),
    surfaceContainerHigh = Color(0xFFDFE8E3),
    surfaceContainerHighest = Color(0xFFD6E0DA),
    outline = Color(0xFF707974),
    outlineVariant = Color(0xFFC0C9C4),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF84DDBB),
    onPrimary = Color(0xFF003829),
    primaryContainer = Color(0xFF00513D),
    onPrimaryContainer = Color(0xFFA2F5D5),
    secondary = Color(0xFFB0CCC0),
    onSecondary = Color(0xFF1C352C),
    secondaryContainer = Color(0xFF334C42),
    onSecondaryContainer = Color(0xFFCBE9DD),
    tertiary = Color(0xFFA7CDDF),
    onTertiary = Color(0xFF0A3445),
    background = Color(0xFF0D1511),
    onBackground = Color(0xFFDDE5E0),
    surface = Color(0xFF0D1511),
    onSurface = Color(0xFFDDE5E0),
    surfaceVariant = Color(0xFF3F4944),
    onSurfaceVariant = Color(0xFFC0C9C4),
    surfaceContainerLowest = Color(0xFF080F0C),
    surfaceContainerLow = Color(0xFF121A16),
    surfaceContainer = Color(0xFF161F1B),
    surfaceContainerHigh = Color(0xFF1C2521),
    surfaceContainerHighest = Color(0xFF27312C),
    outline = Color(0xFF89938D),
    outlineVariant = Color(0xFF3F4944),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
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
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        content = content,
    )
}
