package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
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
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val palette = if (darkTheme) DarkMontraPalette else LightMontraPalette
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalMontraColors provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

