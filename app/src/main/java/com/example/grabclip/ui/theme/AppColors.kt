package com.example.grabclip.ui.theme

import androidx.compose.ui.graphics.Color

object AppColors {
    val danger: Color = Color(0xFFC8402F)
    val divider: Color = Color(0xFFE4DFD8)
    val glowButter: Color = Color(0xFFF9E08A)
    val glowLilac: Color = Color(0xFFD8C8F3)
    val glowMint: Color = Color(0xFFCDEBC4)
    val glowRose: Color = Color(0xFFF7C4D4)
    val glowSky: Color = Color(0xFFBFDCF5)
    val primary: Color = Color(0xFF111111)
    val primaryOn: Color = Color(0xFFFFFFFF)
    val success: Color = Color(0xFF2E7D4F)
    val surfaceBase: Color = Color(0xFFF5F3F0)
    val surfaceCard: Color = Color(0xFFFFFFFF)
    val surfaceInverse: Color = Color(0xFF111111)
    val surfaceSunken: Color = Color(0xFFECE8E2)
    val tagLime: Color = Color(0xFFDFF08A)
    val textDisabled: Color = Color(0xFFB9B4AD)
    val textPrimary: Color = Color(0xFF111111)
    val textSecondary: Color = Color(0xFF6F6B66)
    val textTertiary: Color = Color(0xFF8E8983)

    val iridescent: List<Color> by lazy { listOf(glowButter, glowRose, glowLilac, glowSky, glowMint) }
}
