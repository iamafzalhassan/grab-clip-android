package com.example.grabclip.domain.video

import androidx.compose.runtime.Immutable
import com.example.grabclip.domain.link.VideoLink

private const val NO_CODEC: String = "none"

enum class VideoError { FAILED, LOGIN_REQUIRED, NO_SPACE, OFFLINE, UNAVAILABLE, UNSUPPORTED }

@Immutable
data class VideoStream(val height: Int, val width: Int, val sizeBytes: Long, val audioCodec: String, val container: String, val videoCodec: String) {
    val hasAudio: Boolean get() = audioCodec != NO_CODEC
    val hasVideo: Boolean get() = videoCodec != NO_CODEC

    val resolution: Int get() = if (width > 0 && height > 0) minOf(width, height) else height
}

@Immutable
data class VideoQuality(val isAudioOnly: Boolean, val height: Int, val resolution: Int, val sizeBytes: Long)

@Immutable
data class VideoDetails(val durationSeconds: Int, val title: String, val qualities: List<VideoQuality>, val link: VideoLink)
