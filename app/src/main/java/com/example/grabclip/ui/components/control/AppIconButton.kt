package com.example.grabclip.ui.components.control

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import com.example.grabclip.ui.theme.AppColors
import com.example.grabclip.ui.theme.AppSpacing

@Composable
fun AppIconButton(contentDescription: String, onClick: () -> Unit, icon: ImageVector, modifier: Modifier = Modifier, isInverted: Boolean = false) {
    val shape = RoundedCornerShape(AppSpacing.radiusField)

    Box(
        modifier = modifier
            .size(AppSpacing.iconButton)
            .clip(shape)
            .background(color = if (isInverted) AppColors.primary else AppColors.surfaceCard, shape = shape)
            .clickable(onClick = onClick, role = Role.Button),
        contentAlignment = Alignment.Center,
    ) {
        Icon(modifier = Modifier.size(AppSpacing.icon), contentDescription = contentDescription, imageVector = icon, tint = if (isInverted) AppColors.primaryOn else AppColors.textPrimary)
    }
}
