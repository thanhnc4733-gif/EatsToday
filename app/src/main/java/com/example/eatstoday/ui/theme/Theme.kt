package com.example.eatstoday.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.example.eatstoday.data.model.AppThemeMode
import com.example.eatstoday.data.model.TextSizeOption

private val LightColors = lightColorScheme(
    primary = EatsTodayColors.Primary,
    onPrimary = EatsTodayColors.Surface,
    background = EatsTodayColors.Background,
    onBackground = EatsTodayColors.TextPrimary,
    surface = EatsTodayColors.Surface,
    onSurface = EatsTodayColors.TextPrimary,
    surfaceVariant = EatsTodayColors.Surface,
    onSurfaceVariant = EatsTodayColors.TextSecondary,
    outline = EatsTodayColors.ChipBorder,
    outlineVariant = EatsTodayColors.Border,
    secondaryContainer = EatsTodayColors.Placeholder,
    onSecondaryContainer = EatsTodayColors.OnPlaceholder,
    error = EatsTodayColors.Primary,
)

private val DarkColors = darkColorScheme(
    primary = EatsTodayColors.DarkPrimary,
    onPrimary = EatsTodayColors.DarkOnPrimary,
    background = EatsTodayColors.DarkBackground,
    onBackground = EatsTodayColors.DarkText,
    surface = EatsTodayColors.DarkSurface,
    onSurface = EatsTodayColors.DarkText,
    surfaceVariant = EatsTodayColors.DarkSurface,
    onSurfaceVariant = EatsTodayColors.DarkTextSecondary,
    outline = EatsTodayColors.DarkBorder,
    outlineVariant = EatsTodayColors.DarkBorder,
    secondaryContainer = EatsTodayColors.DarkPlaceholder,
    onSecondaryContainer = EatsTodayColors.DarkOnPlaceholder,
    error = EatsTodayColors.DarkPrimary,
)

@Composable
fun EatsTodayTheme(
    themeMode: AppThemeMode = AppThemeMode.LIGHT,
    textSize: TextSizeOption = TextSizeOption.MEDIUM,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(density.density, density.fontScale * textSize.scale),
    ) {
        MaterialTheme(
            colorScheme = if (dark) DarkColors else LightColors,
            typography = AppTypography,
            content = content,
        )
    }
}
