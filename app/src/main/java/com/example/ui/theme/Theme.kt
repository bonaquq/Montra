package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

enum class AppTheme(val displayName: String, val description: String) {
    OLED("OLED", "Pitch-black dark palette for OLED screens"),
    DARK("Dark Mode", "Refined #1c1c1c charcoal dark palette"),
    LIGHT("Light Theme", "Crisp, bright high-contrast light palette");

    val isDark: Boolean
        get() = this != LIGHT

    companion object {
        fun fromString(value: String?): AppTheme {
            return when (value?.uppercase(java.util.Locale.US)) {
                "OLED" -> OLED
                "DARK", "DARK_MODE" -> DARK
                "LIGHT" -> LIGHT
                else -> DARK
            }
        }
    }
}

private val OledColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF222227),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFA1A1AA),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF222227),
    onSecondaryContainer = Color.White,
    tertiary = MontraIncomeGreen,
    onTertiary = Color.White,
    background = Color(0xFF0D0D11),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF18181B),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF222227),
    onSurfaceVariant = Color(0xFFA1A1AA),
    error = MontraExpenseRed,
    onError = Color.White,
    outline = Color(0xFF27272A)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF303030),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFA1A1AA),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF303030),
    onSecondaryContainer = Color.White,
    tertiary = MontraIncomeGreen,
    onTertiary = Color.White,
    background = Color(0xFF1C1C1C),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF262626),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF303030),
    onSurfaceVariant = Color(0xFFA1A1AA),
    error = MontraExpenseRed,
    onError = Color.White,
    outline = Color(0xFF383838)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0F172A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF1F5F9),
    onPrimaryContainer = Color(0xFF0F172A),
    secondary = Color(0xFF475569),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1F5F9),
    onSecondaryContainer = Color(0xFF0F172A),
    tertiary = MontraIncomeGreen,
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    error = MontraExpenseRed,
    onError = Color.White,
    outline = Color(0xFFE2E8F0)
)

@Composable
fun MyApplicationTheme(
    appTheme: AppTheme = AppTheme.DARK,
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val palette = when (appTheme) {
        AppTheme.OLED -> OledMontraPalette
        AppTheme.DARK -> DarkMontraPalette
        AppTheme.LIGHT -> LightMontraPalette
    }
    val colorScheme = when (appTheme) {
        AppTheme.OLED -> OledColorScheme
        AppTheme.DARK -> DarkColorScheme
        AppTheme.LIGHT -> LightColorScheme
    }

    CompositionLocalProvider(LocalMontraColors provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

