package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Montra Design System Palette Specification
data class MontraColorPalette(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val border: Color,
    val buttonBg: Color,
    val buttonBgActive: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val isDark: Boolean
)

val DarkMontraPalette = MontraColorPalette(
    background = Color(0xFF0D0D11),
    surface = Color(0xFF18181B),
    surfaceElevated = Color(0xFF222227),
    border = Color(0xFF27272A),
    buttonBg = Color(0xFF27272E),
    buttonBgActive = Color(0xFF32323D),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFA1A1AA),
    textMuted = Color(0xFF71717A),
    isDark = true
)

val LightMontraPalette = MontraColorPalette(
    background = Color(0xFFF8FAFC),      // Crisp, clean off-white / light slate canvas
    surface = Color(0xFFFFFFFF),         // Clean white cards and sheets
    surfaceElevated = Color(0xFFF1F5F9), // Elevated subtle chips and item backgrounds
    border = Color(0xFFE2E8F0),          // Subtle hairline border
    buttonBg = Color(0xFFE2E8F0),        // Light button container
    buttonBgActive = Color(0xFFCBD5E1),  // Active / pressed button state
    textPrimary = Color(0xFF0F172A),     // Deep crisp slate navy for primary text
    textSecondary = Color(0xFF475569),   // Medium slate for secondary captions
    textMuted = Color(0xFF94A3B8),       // Soft muted gray
    isDark = false
)

val LocalMontraColors = staticCompositionLocalOf { DarkMontraPalette }

// Dynamic Accessors evaluated in Composables based on current theme
val MontraBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalMontraColors.current.background

val MontraSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalMontraColors.current.surface

val MontraSurfaceElevated: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalMontraColors.current.surfaceElevated

val MontraBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalMontraColors.current.border

val MontraButtonBg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalMontraColors.current.buttonBg

val MontraButtonBgActive: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalMontraColors.current.buttonBgActive

// Text
val MontraTextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalMontraColors.current.textPrimary

val MontraTextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalMontraColors.current.textSecondary

val MontraTextMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalMontraColors.current.textMuted

// Badges & Status
val MontraIncomeGreen = Color(0xFF059669)
val MontraExpenseRed = Color(0xFFEF4444)

// Categories
val CategoryPurchase = Color(0xFFF43F5E)
val CategoryTransfer = Color(0xFF0D9488)
val CategoryRent = Color(0xFF0284C7)
val CategoryFood = Color(0xFFEA580C)
val CategoryTransport = Color(0xFF9333EA)
val CategoryShopping = Color(0xFFDB2777)
val CategoryEntertainment = Color(0xFFCA8A04)
val CategoryUtilities = Color(0xFF0891B2)
val CategoryIncome = Color(0xFF16A34A)
val CategorySalary = CategoryIncome // Legacy compatibility
val CategoryOther = Color(0xFF64748B)

// Compatibility aliases
val AppBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = MontraBackground

val AppSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = MontraSurface

val TextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = MontraTextPrimary

val TextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = MontraTextSecondary

val TextMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = MontraTextMuted

val DividerColor: Color
    @Composable
    @ReadOnlyComposable
    get() = MontraBorder

val PurplePrimary = Color(0xFF8B5CF6)
val PurpleDark = Color(0xFF7C3AED)
val CategoryBills = CategoryRent
val CategoryHealth = Color(0xFF10B981)
val BlueIncomeBadge = Color(0xFF1E293B)
val BlueIncomeIcon = Color(0xFF38BDF8)
val CoralExpenseBadge = Color(0xFF2D1B1B)
val CoralExpenseIcon = Color(0xFFEF4444)
val DarkInsightBanner = Color(0xFF222227)

// Dark Theme Variants
val DarkBackground = Color(0xFF0D0D11)
val DarkSurface = Color(0xFF18181B)
val DarkSurfaceVariant = Color(0xFF222227)
val DarkTextPrimary = Color(0xFFFFFFFF)
val DarkTextSecondary = Color(0xFFA1A1AA)
