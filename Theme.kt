package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val ApexDarkColorScheme = darkColorScheme(
    primary = RacingAmber,
    onPrimary = ObsidianBg,
    primaryContainer = NeonOrange,
    onPrimaryContainer = TextPrimary,
    secondary = CyanTelemetry,
    onSecondary = ObsidianBg,
    secondaryContainer = AsphaltSurfaceVariant,
    onSecondaryContainer = CyanTelemetry,
    tertiary = EmeraldCash,
    onTertiary = ObsidianBg,
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = CarbonSurface,
    onSurface = TextPrimary,
    surfaceVariant = AsphaltSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = SteelBorder,
    error = CrimsonRedline,
    onError = TextPrimary
)

val ApexShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp)
)

@Composable
fun ApexRiderTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ApexDarkColorScheme,
        typography = AppTypography,
        shapes = ApexShapes,
        content = content
    )
}
