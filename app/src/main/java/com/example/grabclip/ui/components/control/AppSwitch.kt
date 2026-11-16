package com.example.grabclip.ui.components.control

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import com.example.grabclip.ui.theme.AppColors
import com.example.grabclip.ui.theme.AppSpacing

@Composable
fun AppSwitch(isChecked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val thumbSize = AppSpacing.switchHeight - AppSpacing.xs * 2
    val thumbPosition by animateFloatAsState(label = "switchThumb", targetValue = if (isChecked) 1f else 0f)
    val trackColor by animateColorAsState(label = "switchTrack", targetValue = if (isChecked) AppColors.primary else AppColors.surfaceSunken)

    Box(
        modifier = modifier
            .size(height = AppSpacing.switchHeight, width = AppSpacing.switchWidth)
            .clip(CircleShape)
            .background(color = trackColor, shape = CircleShape)
            .toggleable(onValueChange = onCheckedChange, role = Role.Switch, value = isChecked)
            .padding(AppSpacing.xs),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(modifier = Modifier.offset(x = (AppSpacing.switchWidth - AppSpacing.switchHeight) * thumbPosition).size(thumbSize).background(color = if (isChecked) AppColors.primaryOn else AppColors.surfaceCard, shape = CircleShape))
    }
}
