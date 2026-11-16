package com.example.grabclip.ui.components.display

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.grabclip.ui.theme.AppColors
import com.example.grabclip.ui.theme.AppSpacing
import com.example.grabclip.ui.theme.AppTextStyles

@Composable
fun AppTag(label: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier.background(color = AppColors.tagLime, shape = RoundedCornerShape(AppSpacing.radiusTag)).padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
        maxLines = 1,
        style = AppTextStyles.overline,
        text = label,
    )
}
