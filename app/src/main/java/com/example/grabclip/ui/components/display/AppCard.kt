package com.example.grabclip.ui.components.display

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.grabclip.ui.theme.AppColors
import com.example.grabclip.ui.theme.AppSpacing

@Composable
fun AppTray(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier.fillMaxWidth().background(color = AppColors.surfaceSunken, shape = RoundedCornerShape(AppSpacing.radiusTray)).padding(AppSpacing.trayPadding),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        content = content,
    )
}

@Composable
fun AppCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier.fillMaxWidth().background(color = AppColors.surfaceCard, shape = RoundedCornerShape(AppSpacing.radiusCard)).padding(AppSpacing.cardPadding),
        content = content,
    )
}
