package com.denisp.pillstracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.denisp.pillstracker.model.ThemeMode

private val LightColors = lightColorScheme(
    primary = Color(0xFF087F8C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC9F0F2),
    onPrimaryContainer = Color(0xFF002F35),
    secondary = Color(0xFF526A78),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCEAF2),
    onSecondaryContainer = Color(0xFF102A37),
    tertiary = Color(0xFF58709B),
    onTertiary = Color.White,
    background = Color(0xFFF2F7FB),
    onBackground = Color(0xFF15202B),
    surface = Color(0xFFF2F7FB),
    onSurface = Color(0xFF15202B),
    surfaceVariant = Color(0xFFDCE5EC),
    onSurfaceVariant = Color(0xFF46545F),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF9FCFE),
    surfaceContainer = Color(0xFFF0F5F8),
    surfaceContainerHigh = Color(0xFFE8EFF4),
    surfaceContainerHighest = Color(0xFFDDE7ED),
    outline = Color(0xFF71808B),
    outlineVariant = Color(0xFFC7D3DB),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF65D4DC),
    onPrimary = Color(0xFF00363B),
    primaryContainer = Color(0xFF124C55),
    onPrimaryContainer = Color(0xFFA8EDF1),
    secondary = Color(0xFFB7CBD7),
    onSecondary = Color(0xFF23333D),
    secondaryContainer = Color(0xFF344955),
    onSecondaryContainer = Color(0xFFD3E7F2),
    tertiary = Color(0xFFC0C9FF),
    onTertiary = Color(0xFF27305F),
    background = Color(0xFF0B1622),
    onBackground = Color(0xFFE5EDF3),
    surface = Color(0xFF0B1622),
    onSurface = Color(0xFFE5EDF3),
    surfaceVariant = Color(0xFF3E4A54),
    onSurfaceVariant = Color(0xFFBEC9D1),
    surfaceContainerLowest = Color(0xFF111D29),
    surfaceContainerLow = Color(0xFF152331),
    surfaceContainer = Color(0xFF192936),
    surfaceContainerHigh = Color(0xFF20313E),
    surfaceContainerHighest = Color(0xFF293B48),
    outline = Color(0xFF89959E),
    outlineVariant = Color(0xFF3A4B58),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

private val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 35.sp,
        letterSpacing = (-0.4).sp,
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 23.sp,
        lineHeight = 29.sp,
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 23.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(AppRadii.Small),
    small = RoundedCornerShape(AppRadii.Small),
    medium = RoundedCornerShape(AppRadii.Control),
    large = RoundedCornerShape(AppRadii.Card),
    extraLarge = RoundedCornerShape(AppRadii.Dashboard),
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
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
