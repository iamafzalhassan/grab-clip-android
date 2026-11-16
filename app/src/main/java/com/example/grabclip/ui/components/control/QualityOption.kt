package com.example.grabclip.ui.components.control

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.example.grabclip.R
import com.example.grabclip.ui.theme.AppColors
import com.example.grabclip.ui.theme.AppSpacing
import com.example.grabclip.ui.theme.AppTextStyles

@Composable
fun QualityOption(isSelected: Boolean, label: String, meta: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(AppSpacing.radiusCard)
    val checkShape = RoundedCornerShape(AppSpacing.radiusTag)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = AppSpacing.controlHeight)
            .clip(shape)
            .background(color = AppColors.surfaceCard, shape = shape)
            .border(color = if (isSelected) AppColors.primary else Color.Transparent, shape = shape, width = AppSpacing.hairline)
            .selectable(onClick = onClick, role = Role.RadioButton, selected = isSelected)
            .padding(horizontal = AppSpacing.cardPadding),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis, style = AppTextStyles.listPrimary, text = label)
        Text(maxLines = 1, style = AppTextStyles.listMeta, text = meta)
        Box(modifier = Modifier.size(AppSpacing.checkBox).background(color = if (isSelected) AppColors.primary else AppColors.surfaceSunken, shape = checkShape), contentAlignment = Alignment.Center) {
            if (isSelected) {
                Icon(modifier = Modifier.size(AppSpacing.lg), contentDescription = stringResource(R.string.quality_selected), imageVector = Icons.Outlined.Check, tint = AppColors.primaryOn)
            }
        }
    }
}
