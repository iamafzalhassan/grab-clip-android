package com.example.grabclip.ui.components.display

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.grabclip.ui.theme.AppColors
import com.example.grabclip.ui.theme.AppSpacing
import com.example.grabclip.ui.theme.AppTextStyles

private const val FULL_TURN: Float = 360f
private const val TOP_ANGLE: Float = -90f

private const val FILL_MILLIS: Int = 400

@Composable
fun ProgressRing(fraction: Float, caption: String, label: String, modifier: Modifier = Modifier) {
    val filled by animateFloatAsState(animationSpec = tween(durationMillis = FILL_MILLIS), label = "progressRing", targetValue = fraction.coerceIn(0f, 1f))
    val stops = remember { AppColors.iridescent + AppColors.iridescent.first() }

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(AppSpacing.progressRing).progressSemantics(filled), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = AppSpacing.ringStroke.toPx()
                val arcSize = Size(height = size.height - stroke, width = size.width - stroke)
                val topLeft = Offset(x = stroke / 2f, y = stroke / 2f)

                drawArc(color = AppColors.surfaceSunken, size = arcSize, startAngle = 0f, style = Stroke(width = stroke), sweepAngle = FULL_TURN, topLeft = topLeft, useCenter = false)
                if (filled > 0f) {
                    rotate(degrees = TOP_ANGLE) {
                        drawArc(brush = Brush.sweepGradient(stops), size = arcSize, startAngle = 0f, style = Stroke(cap = StrokeCap.Round, width = stroke), sweepAngle = FULL_TURN * filled, topLeft = topLeft, useCenter = false)
                    }
                }
            }
            Text(maxLines = 1, style = AppTextStyles.metric, text = label)
        }
        Spacer(modifier = Modifier.height(AppSpacing.md))
        Text(modifier = Modifier.fillMaxWidth(), maxLines = 1, overflow = TextOverflow.Ellipsis, style = AppTextStyles.listMeta.copy(textAlign = TextAlign.Center), text = caption)
    }
}
