package com.example.grabclip.ui.components.display

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.example.grabclip.ui.theme.AppColors
import com.example.grabclip.ui.theme.AppSpacing
import kotlin.math.cos
import kotlin.math.sin

private const val DRIFT_RATIO: Float = 0.05f
private const val FADE_START_RATIO: Float = 0.55f
private const val FULL_TURN: Float = 360f
private const val GLOW_ALPHA: Float = 0.85f

private const val DRIFT_MILLIS: Int = 14_000

private val glowSpots: List<GlowSpot> = listOf(
    GlowSpot(centerX = 0.5f, centerY = 0.45f, phase = 0f, radius = 0.55f),
    GlowSpot(centerX = 0.22f, centerY = 0.25f, phase = 1.3f, radius = 0.42f),
    GlowSpot(centerX = 0.8f, centerY = 0.22f, phase = 2.6f, radius = 0.42f),
    GlowSpot(centerX = 0.12f, centerY = 0.62f, phase = 3.9f, radius = 0.38f),
    GlowSpot(centerX = 0.88f, centerY = 0.6f, phase = 5.2f, radius = 0.36f),
)

private data class GlowSpot(val centerX: Float, val centerY: Float, val phase: Float, val radius: Float)

@Composable
fun IridescentGlow(isDrifting: Boolean, modifier: Modifier = Modifier) {
    val turn = remember { Animatable(0f) }

    LaunchedEffect(isDrifting) {
        val motionScale = coroutineContext[MotionDurationScale]?.scaleFactor ?: 1f
        if (isDrifting && motionScale > 0f) {
            while (true) {
                turn.animateTo(animationSpec = tween(durationMillis = DRIFT_MILLIS, easing = LinearEasing), targetValue = turn.value + FULL_TURN)
                turn.snapTo(turn.value % FULL_TURN)
            }
        }
    }

    Canvas(modifier = modifier.fillMaxWidth().height(AppSpacing.glowHeight).clipToBounds()) {
        val drift = size.width * DRIFT_RATIO
        val radians = Math.toRadians(turn.value.toDouble()).toFloat()

        glowSpots.forEachIndexed { index, spot ->
            val color = AppColors.iridescent[index]
            val center = Offset(x = size.width * spot.centerX + drift * cos(radians + spot.phase), y = size.height * spot.centerY + drift * sin(radians + spot.phase))
            val radius = size.width * spot.radius

            drawCircle(brush = Brush.radialGradient(center = center, colors = listOf(color.copy(alpha = GLOW_ALPHA), color.copy(alpha = 0f)), radius = radius), center = center, radius = radius)
        }
        drawRect(brush = Brush.verticalGradient(colors = listOf(AppColors.surfaceBase.copy(alpha = 0f), AppColors.surfaceBase), endY = size.height, startY = size.height * FADE_START_RATIO))
    }
}
