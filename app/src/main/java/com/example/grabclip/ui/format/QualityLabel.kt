package com.example.grabclip.ui.format

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.grabclip.R
import com.example.grabclip.domain.video.QualityPreference
import com.example.grabclip.domain.video.VideoQuality

@Composable
fun qualityLabel(quality: VideoQuality): String = when {
    quality.isAudioOnly -> stringResource(R.string.quality_audio_only)
    quality.resolution <= 0 -> stringResource(R.string.quality_best_available)
    else -> stringResource(R.string.quality_resolution, quality.resolution)
}

@Composable
fun preferenceLabel(preference: Int): String = when (preference) {
    QualityPreference.AUDIO_ONLY -> stringResource(R.string.settings_quality_audio)
    else -> stringResource(R.string.quality_resolution, preference)
}
