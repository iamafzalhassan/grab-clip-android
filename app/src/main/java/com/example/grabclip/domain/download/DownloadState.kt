package com.example.grabclip.domain.download

import androidx.compose.runtime.Immutable
import com.example.grabclip.domain.video.VideoError
import com.example.grabclip.domain.video.VideoQuality

sealed interface DownloadState {
    data class Completed(val video: DownloadedVideo) : DownloadState

    data class Failed(val error: VideoError) : DownloadState

    data object Idle : DownloadState

    data class Running(val fraction: Float) : DownloadState
}

@Immutable
data class DownloadedVideo(val isKept: Boolean, val sizeBytes: Long, val mimeType: String, val path: String, val title: String, val uri: String, val quality: VideoQuality)
