package com.example.myapplication.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import com.example.myapplication.ui.theme.Blue700
import com.example.myapplication.ui.theme.Cyan400
import com.example.myapplication.ui.theme.Blue500
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedBackground() {
    // Fond volontairement fixe : aucune forme ni animation derrière le contenu.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    )
    return

    val infiniteTransition = rememberInfiniteTransition(label = "background")
    
    // Animation pour le mouvement circulaire des formes
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "angle"
    )

    // Animation pour la variation de taille
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    Canvas(modifier = Modifier
        .fillMaxSize()
        .blur(80.dp) 
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerX = canvasWidth / 2
        val centerY = canvasHeight / 2

        // Cercle 1 : Bleu Profond (en haut à gauche)
        drawCircle(
            color = Blue700.copy(alpha = 0.35f),
            radius = (canvasWidth * 0.7f) * scale,
            center = Offset(
                x = centerX + (canvasWidth * 0.3f) * cos(Math.toRadians(angle.toDouble())).toFloat(),
                y = centerY + (canvasHeight * 0.2f) * sin(Math.toRadians(angle.toDouble())).toFloat()
            )
        )

        // Cercle 2 : Cyan (en bas à droite)
        drawCircle(
            color = Cyan400.copy(alpha = 0.25f),
            radius = (canvasWidth * 0.6f) * scale,
            center = Offset(
                x = centerX - (canvasWidth * 0.4f) * cos(Math.toRadians(angle.toDouble() + 180)).toFloat(),
                y = centerY - (canvasHeight * 0.3f) * sin(Math.toRadians(angle.toDouble() + 180)).toFloat()
            )
        )

        // Cercle 3 : Bleu Ciel (éclat central)
        drawCircle(
            color = Blue500.copy(alpha = 0.15f),
            radius = (canvasWidth * 0.4f),
            center = Offset(
                x = centerX + (canvasWidth * 0.2f) * sin(Math.toRadians(angle.toDouble() * 2)).toFloat(),
                y = centerY + (canvasHeight * 0.2f) * cos(Math.toRadians(angle.toDouble() * 2)).toFloat()
            )
        )
    }
}
