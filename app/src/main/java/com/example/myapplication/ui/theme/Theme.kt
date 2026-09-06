package com.example.myapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Blue500,
    secondary = Cyan400,
    tertiary = Blue400,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextDark,
    onSurface = TextDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = Color(0xFF1A2A44)
)

private val LightColorScheme = lightColorScheme(
    primary = Blue600,
    secondary = Cyan500,
    tertiary = Blue500,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextLight,
    onSurface = TextLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0xFFC8D8E8)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
