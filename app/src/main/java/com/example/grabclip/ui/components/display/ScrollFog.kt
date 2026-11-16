package com.example.grabclip.ui.components.display

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.example.grabclip.ui.theme.AppColors
import com.example.grabclip.ui.theme.AppSpacing

private const val MID_ALPHA: Float = 0.7f

@Composable
fun ScrollFog(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().height(AppSpacing.fadingEdge).background(Brush.verticalGradient(listOf(AppColors.surfaceBase.copy(alpha = 0f), AppColors.surfaceBase.copy(alpha = MID_ALPHA), AppColors.surfaceBase))))
}
