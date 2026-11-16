package com.example.grabclip.feature.grab

import androidx.compose.runtime.Immutable
import com.example.grabclip.domain.download.DownloadedVideo
import com.example.grabclip.domain.link.VideoLink
import com.example.grabclip.domain.video.VideoDetails
import com.example.grabclip.domain.video.VideoError
import com.example.grabclip.domain.video.VideoQuality

enum class GrabPhase { CHOOSING, DOWNLOADING, EMPTY, FAILED, FETCHING, READY }

sealed interface GrabEvent {
    data object CancelDownload : GrabEvent

    data class ClipboardRead(val isManual: Boolean, val text: String?) : GrabEvent

    data object DiscardVideo : GrabEvent

    data object DismissFeedback : GrabEvent

    data object Download : GrabEvent

    data object Retry : GrabEvent

    data object Returned : GrabEvent

    data class SelectQuality(val quality: VideoQuality) : GrabEvent

    data object ShareFailed : GrabEvent
}

sealed interface GrabFeedback {
    data object DownloadCancelled : GrabFeedback

    data class DownloadFailed(val error: VideoError) : GrabFeedback

    data object LinkNotFound : GrabFeedback

    data object ShareFailed : GrabFeedback

    data object VideoDeleted : GrabFeedback

    data object VideoKept : GrabFeedback
}

@Immutable
data class GrabUiState(
    val progress: Float = 0f,
    val video: DownloadedVideo? = null,
    val feedback: GrabFeedback? = null,
    val phase: GrabPhase = GrabPhase.EMPTY,
    val details: VideoDetails? = null,
    val error: VideoError? = null,
    val link: VideoLink? = null,
    val selected: VideoQuality? = null,
)
