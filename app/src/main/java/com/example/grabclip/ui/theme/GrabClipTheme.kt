package com.example.grabclip.ui.theme

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private const val SELECTION_ALPHA: Float = 0.16f

private val GrabClipColorScheme: ColorScheme = lightColorScheme(
    background = AppColors.surfaceBase,
    error = AppColors.danger,
    errorContainer = AppColors.danger,
    inverseOnSurface = AppColors.primaryOn,
    inverseSurface = AppColors.surfaceInverse,
    onBackground = AppColors.textPrimary,
    onError = AppColors.primaryOn,
    onErrorContainer = AppColors.primaryOn,
    onPrimary = AppColors.primaryOn,
    onPrimaryContainer = AppColors.primaryOn,
    onSecondary = AppColors.primaryOn,
    onSurface = AppColors.textPrimary,
    onSurfaceVariant = AppColors.textSecondary,
    outline = AppColors.divider,
    outlineVariant = AppColors.divider,
    primary = AppColors.primary,
    primaryContainer = AppColors.primary,
    scrim = AppColors.primary,
    secondary = AppColors.primary,
    surface = AppColors.surfaceCard,
    surfaceContainer = AppColors.surfaceCard,
    surfaceContainerHigh = AppColors.surfaceCard,
    surfaceContainerHighest = AppColors.surfaceSunken,
    surfaceContainerLow = AppColors.surfaceBase,
    surfaceContainerLowest = AppColors.surfaceBase,
    surfaceVariant = AppColors.surfaceSunken,
    tertiary = AppColors.primary,
)

private val GrabClipTypography: Typography = Typography(
    bodyLarge = AppTextStyles.body,
    bodyMedium = AppTextStyles.listSecondary,
    bodySmall = AppTextStyles.errorHint,
    displaySmall = AppTextStyles.display,
    headlineSmall = AppTextStyles.videoTitle,
    labelLarge = AppTextStyles.button,
    labelSmall = AppTextStyles.overline,
    titleLarge = AppTextStyles.sectionHeading,
    titleMedium = AppTextStyles.screenTitle,
    titleSmall = AppTextStyles.listPrimary,
)

@Composable
fun GrabClipTheme(content: @Composable () -> Unit) {
    val selectionColors = TextSelectionColors(backgroundColor = AppColors.primary.copy(alpha = SELECTION_ALPHA), handleColor = AppColors.primary)

    CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
        MaterialTheme(colorScheme = GrabClipColorScheme, typography = GrabClipTypography, content = content)
    }
}
