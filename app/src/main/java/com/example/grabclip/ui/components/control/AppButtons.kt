package com.example.grabclip.ui.components.control

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.example.grabclip.ui.components.feedback.ActivityIndicator
import com.example.grabclip.ui.theme.AppColors
import com.example.grabclip.ui.theme.AppSpacing
import com.example.grabclip.ui.theme.AppTextStyles

@Composable
fun PrimaryButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, isEnabled: Boolean = true, isLoading: Boolean = false) {
    val isActive = isEnabled && !isLoading

    Button(
        modifier = modifier.heightIn(min = AppSpacing.buttonHeight),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.primary,
            contentColor = AppColors.primaryOn,
            disabledContainerColor = if (isLoading) AppColors.primary else AppColors.surfaceSunken,
            disabledContentColor = AppColors.textDisabled,
        ),
        contentPadding = PaddingValues(horizontal = AppSpacing.lg),
        elevation = null,
        enabled = isActive,
        onClick = onClick,
        shape = RoundedCornerShape(AppSpacing.radiusButton),
    ) {
        if (isLoading) {
            ActivityIndicator(color = AppColors.primaryOn)
        } else {
            Text(maxLines = 1, overflow = TextOverflow.Ellipsis, style = AppTextStyles.button.copy(color = if (isActive) AppColors.primaryOn else AppColors.textDisabled), text = label)
        }
    }
}

@Composable
fun SecondaryButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, isEnabled: Boolean = true, isLoading: Boolean = false) =
    OutlinedActionButton(modifier = modifier, accent = AppColors.primary, isEnabled = isEnabled, isLoading = isLoading, label = label, onClick = onClick)

@Composable
fun DangerButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, isEnabled: Boolean = true) =
    OutlinedActionButton(modifier = modifier, accent = AppColors.danger, isEnabled = isEnabled, isLoading = false, label = label, onClick = onClick)

@Composable
private fun OutlinedActionButton(isEnabled: Boolean, isLoading: Boolean, label: String, onClick: () -> Unit, accent: Color, modifier: Modifier = Modifier) {
    val isActive = isEnabled && !isLoading

    OutlinedButton(
        modifier = modifier.heightIn(min = AppSpacing.buttonHeight),
        border = BorderStroke(AppSpacing.hairline, if (isEnabled) accent else AppColors.divider),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = AppColors.surfaceCard,
            contentColor = accent,
            disabledContainerColor = if (isLoading) AppColors.surfaceCard else Color.Transparent,
            disabledContentColor = AppColors.textDisabled,
        ),
        contentPadding = PaddingValues(horizontal = AppSpacing.lg),
        enabled = isActive,
        onClick = onClick,
        shape = RoundedCornerShape(AppSpacing.radiusButton),
    ) {
        if (isLoading) {
            ActivityIndicator(color = AppColors.textSecondary)
        } else {
            Text(maxLines = 1, overflow = TextOverflow.Ellipsis, style = AppTextStyles.button.copy(color = if (isEnabled) accent else AppColors.textDisabled), text = label)
        }
    }
}
