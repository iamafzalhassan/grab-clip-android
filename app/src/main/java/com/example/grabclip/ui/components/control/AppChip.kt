package com.example.grabclip.ui.components.control

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import com.example.grabclip.ui.theme.AppColors
import com.example.grabclip.ui.theme.AppSpacing
import com.example.grabclip.ui.theme.AppTextStyles

@Composable
fun AppChip(isSelected: Boolean, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(AppSpacing.radiusField)

    Box(
        modifier = modifier
            .height(AppSpacing.chipHeight)
            .clip(shape)
            .background(color = if (isSelected) AppColors.primary else AppColors.surfaceBase, shape = shape)
            .selectable(onClick = onClick, role = Role.RadioButton, selected = isSelected)
            .padding(horizontal = AppSpacing.md),
        contentAlignment = Alignment.Center,
    ) {
        Text(maxLines = 1, style = AppTextStyles.listMeta.copy(color = if (isSelected) AppColors.primaryOn else AppColors.textPrimary), text = label)
    }
}
