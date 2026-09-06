package com.example.myapplication.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.glassCard(
    cornerRadius: Dp = 16.dp,
    borderWidth: Dp = 1.dp
): Modifier {
    val isDark = isSystemInDarkTheme()
    
    // Adaptations pour le mode clair vs sombre
    val backgroundColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.7f)
    val borderAlphaStart = if (isDark) 0.15f else 0.4f
    val borderAlphaEnd = if (isDark) 0.05f else 0.1f

    return this
        .clip(RoundedCornerShape(cornerRadius))
        .background(backgroundColor)
        .border(
            width = borderWidth,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = borderAlphaStart),
                    Color.White.copy(alpha = borderAlphaEnd)
                )
            ),
            shape = RoundedCornerShape(cornerRadius)
        )
}
