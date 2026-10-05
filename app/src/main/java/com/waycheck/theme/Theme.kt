package com.waycheck.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class AppThemeMode {
    AMOLED_BLACK,
    CLEAN_LIGHT
}

data class DuxAppColors(
    val background: Color,
    val surface: Color,
    val card: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accentYellow: Color,
    val accentGreen: Color,
    val accentRed: Color,
    val isDark: Boolean
)

val DarkDuxColors = DuxAppColors(
    background = DuxPureBlack,
    surface = DuxSurfaceBlack,
    card = DuxCardBlack,
    border = DuxBorderBlack,
    textPrimary = DuxTextPrimaryDark,
    textSecondary = DuxTextSecondaryDark,
    accentYellow = DuxYellow,
    accentGreen = DuxGreen,
    accentRed = DuxRed,
    isDark = true
)

val LightDuxColors = DuxAppColors(
    background = DuxLightBg,
    surface = DuxLightSurface,
    card = DuxLightCard,
    border = DuxLightBorder,
    textPrimary = DuxLightTextPrimary,
    textSecondary = DuxLightTextSecondary,
    accentYellow = DuxYellow,
    accentGreen = DuxGreen,
    accentRed = DuxRed,
    isDark = false
)

val LocalDuxColors = staticCompositionLocalOf { DarkDuxColors }

private val MaterialDarkColorScheme = darkColorScheme(
    primary = DuxYellow,
    onPrimary = Color.Black,
    secondary = DuxGreen,
    onSecondary = Color.Black,
    background = DuxPureBlack,
    surface = DuxCardBlack,
    onBackground = Color.White,
    onSurface = Color.White
)

private val MaterialLightColorScheme = lightColorScheme(
    primary = DuxYellow,
    onPrimary = Color.Black,
    secondary = DuxGreen,
    onSecondary = Color.Black,
    background = DuxLightBg,
    surface = DuxLightCard,
    onBackground = DuxLightTextPrimary,
    onSurface = DuxLightTextPrimary
)

@Composable
fun WayCheckTheme(
    themeMode: AppThemeMode = AppThemeMode.AMOLED_BLACK,
    content: @Composable () -> Unit
) {
    val colors = if (themeMode == AppThemeMode.AMOLED_BLACK) DarkDuxColors else LightDuxColors
    val materialScheme = if (themeMode == AppThemeMode.AMOLED_BLACK) MaterialDarkColorScheme else MaterialLightColorScheme

    CompositionLocalProvider(LocalDuxColors provides colors) {
        MaterialTheme(
            colorScheme = materialScheme,
            typography = Typography,
            content = content
        )
    }
}
