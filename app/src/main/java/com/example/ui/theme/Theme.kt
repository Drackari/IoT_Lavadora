package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Slate950,
    primaryContainer = Slate800,
    onPrimaryContainer = ElectricBlue,
    secondary = CyanAccent,
    onSecondary = Slate950,
    secondaryContainer = DeepNavy,
    onSecondaryContainer = Slate100,
    tertiary = WaterBlue,
    onTertiary = Slate950,
    background = Slate950,
    onBackground = Slate100,
    surface = Slate900,
    onSurface = Slate100,
    surfaceVariant = Slate850,
    onSurfaceVariant = Slate300,
    outline = Slate700,
    outlineVariant = Slate800,
    error = EmergencyRed,
    onError = Color.White,
    errorContainer = EmergencyRedContainer,
    onErrorContainer = Slate100
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // IoT controller interface is in modern slate/blue dark mode
    dynamicColor: Boolean = false, // Keep consistent high-contrast slate/blue styling
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
