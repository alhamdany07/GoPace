package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GoPaceGreenLight,
    onPrimary = Color.White,
    primaryContainer = GoPaceGreenDark,
    onPrimaryContainer = Color.White,
    secondary = GoPaceBlue,
    tertiary = GoPaceOrange,
    background = Color(0xFF121416),
    surface = Color(0xFF1E2124),
    onBackground = Color(0xFFEDEDED),
    onSurface = Color(0xFFEDEDED),
    surfaceVariant = Color(0xFF282C30),
    outline = Color(0xFF3E4348)
)

private val LightColorScheme = lightColorScheme(
    primary = GoPaceGreen,
    onPrimary = Color.White,
    primaryContainer = GoPaceGreenContainer,
    onPrimaryContainer = GoPaceGreenDark,
    secondary = GoPaceBlue,
    secondaryContainer = GoPaceBlueContainer,
    tertiary = GoPaceOrange,
    tertiaryContainer = GoPaceOrangeContainer,
    background = SoftBackground,
    surface = SoftSurface,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = SoftCardHeader,
    outline = SoftCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our signature GoPace brand colors for consistent branding
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
