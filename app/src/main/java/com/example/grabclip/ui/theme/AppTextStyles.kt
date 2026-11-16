package com.example.grabclip.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.grabclip.R

object AppTextStyles {
    private val sfProDisplayFamily: FontFamily = FontFamily(
        Font(R.font.sf_pro_display_regular, FontWeight.Normal),
        Font(R.font.sf_pro_display_medium, FontWeight.Medium),
        Font(R.font.sf_pro_display_bold, FontWeight.Bold),
    )

    val body: TextStyle = style(color = AppColors.textPrimary, height = 1.35f, size = 15, weight = FontWeight.Normal)
    val button: TextStyle = style(color = AppColors.primaryOn, height = 1.2f, size = 15, weight = FontWeight.Medium)
    val display: TextStyle = style(color = AppColors.textPrimary, height = 1.15f, size = 28, tracking = -0.3f, weight = FontWeight.Normal)
    val errorHint: TextStyle = style(color = AppColors.danger, height = 1.3f, size = 12, weight = FontWeight.Medium)
    val listMeta: TextStyle = style(color = AppColors.textSecondary, height = 1.3f, size = 13, tabular = true, weight = FontWeight.Normal)
    val listPrimary: TextStyle = style(color = AppColors.textPrimary, height = 1.3f, size = 15, weight = FontWeight.Medium)
    val listSecondary: TextStyle = style(color = AppColors.textSecondary, height = 1.3f, size = 13, weight = FontWeight.Normal)
    val metric: TextStyle = style(color = AppColors.textPrimary, height = 1.1f, size = 40, tabular = true, tracking = -0.5f, weight = FontWeight.Normal)
    val overline: TextStyle = style(color = AppColors.textPrimary, height = 1.2f, size = 10, tracking = 0.6f, weight = FontWeight.Bold)
    val screenTitle: TextStyle = style(color = AppColors.textPrimary, height = 1.2f, size = 15, weight = FontWeight.Medium)
    val sectionHeading: TextStyle = style(color = AppColors.textPrimary, height = 1.2f, size = 20, weight = FontWeight.Normal)
    val snack: TextStyle = style(color = AppColors.primaryOn, height = 1.3f, size = 14, weight = FontWeight.Medium)
    val videoTitle: TextStyle = style(color = AppColors.textPrimary, height = 1.25f, size = 22, tracking = -0.2f, weight = FontWeight.Normal)

    private fun style(height: Float, size: Int, color: Color, weight: FontWeight, tabular: Boolean = false, tracking: Float = 0f): TextStyle = TextStyle(
        color = color,
        fontFamily = sfProDisplayFamily,
        fontFeatureSettings = if (tabular) "tnum" else null,
        fontSize = size.sp,
        fontWeight = weight,
        letterSpacing = tracking.sp,
        lineHeight = (size * height).sp,
    )
}
