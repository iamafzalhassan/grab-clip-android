package com.example.grabclip.feature.grab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabclip.common.STATE_STOP_TIMEOUT_MILLIS
import com.example.grabclip.data.local.SettingsPreferences
import com.example.grabclip.data.remote.ExtractionException
import com.example.grabclip.data.repository.VideoRepository
import com.example.grabclip.domain.download.DownloadState
import com.example.grabclip.domain.link.VideoLink
import com.example.grabclip.domain.link.VideoLinkParser
import com.example.grabclip.domain.video.QualityCatalog
import com.example.grabclip.domain.video.VideoDetails
import com.example.grabclip.domain.video.VideoError
import com.example.grabclip.domain.video.VideoQuality
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private data class GrabLocalState(
    val isFetching: Boolean = false,
    val feedback: GrabFeedback? = null,
    val details: VideoDetails? = null,
    val error: VideoError? = null,
    val link: VideoLink? = null,
    val selected: VideoQuality? = null,
)

class GrabViewModel(private val settings: SettingsPreferences, private val repository: VideoRepository) : ViewModel() {
    private val local: MutableStateFlow<GrabLocalState> = MutableStateFlow(GrabLocalState())

    val state: StateFlow<GrabUiState> = combine(local, repository.download) { current, download -> toUiState(current = current, download = download) }
        .stateIn(initialValue = GrabUiState(), scope = viewModelScope, started = SharingStarted.WhileSubscribed(STATE_STOP_TIMEOUT_MILLIS))

    private var wasDownloading: Boolean = false

    private var fetchJob: Job? = null

    init {
        viewModelScope.launch { repository.download.collect { download -> onDownloadChanged(download) } }
    }

    fun onEvent(event: GrabEvent) {
        when (event) {
            GrabEvent.CancelDownload -> cancelDownload()
            is GrabEvent.ClipboardRead -> onClipboardRead(isManual = event.isManual, text = event.text)
            GrabEvent.DiscardVideo -> discardVideo()
            GrabEvent.DismissFeedback -> local.update { current -> current.copy(feedback = null) }
            GrabEvent.Download -> startDownload()
            GrabEvent.Retry -> retry()
            GrabEvent.Returned -> completeShare()
            is GrabEvent.SelectQuality -> local.update { current -> current.copy(selected = event.quality) }
            GrabEvent.ShareFailed -> local.update { current -> current.copy(feedback = GrabFeedback.ShareFailed) }
        }
    }

    private fun toUiState(current: GrabLocalState, download: DownloadState): GrabUiState = GrabUiState(
        progress = (download as? DownloadState.Running)?.fraction ?: 0f,
        video = (download as? DownloadState.Completed)?.video,
        feedback = current.feedback,
        phase = when {
            download is DownloadState.Running -> GrabPhase.DOWNLOADING
            download is DownloadState.Completed -> GrabPhase.READY
            current.isFetching -> GrabPhase.FETCHING
            current.error != null -> GrabPhase.FAILED
            current.details != null -> GrabPhase.CHOOSING
            else -> GrabPhase.EMPTY
        },
        details = current.details,
        error = current.error,
        link = current.link,
        selected = current.selected,
    )

    private fun onDownloadChanged(download: DownloadState) {
        val hasJustFinished = wasDownloading
        wasDownloading = download is DownloadState.Running
        when {
            download is DownloadState.Failed -> {
                local.update { current -> current.copy(feedback = GrabFeedback.DownloadFailed(download.error)) }
                repository.acknowledgeFailure()
            }
            download is DownloadState.Completed && hasJustFinished && download.video.isKept -> local.update { current -> current.copy(feedback = GrabFeedback.VideoKept) }
        }
    }

    private fun cancelDownload() {
        repository.cancelDownload()
        local.update { current -> current.copy(feedback = GrabFeedback.DownloadCancelled) }
    }

    private fun onClipboardRead(isManual: Boolean, text: String?) {
        val link = VideoLinkParser.parse(text)
        when {
            repository.download.value is DownloadState.Running -> Unit
            link == null -> if (isManual) local.update { current -> current.copy(feedback = GrabFeedback.LinkNotFound) }
            !isManual && link.url == local.value.link?.url -> Unit
            else -> fetch(link)
        }
    }

    private fun discardVideo() {
        viewModelScope.launch {
            repository.discard()
            local.update { current -> GrabLocalState(link = current.link) }
        }
    }

    private fun startDownload() {
        val current = local.value
        val details = current.details ?: return
        val quality = current.selected ?: return
        repository.startDownload(details = details, quality = quality)
    }

    private fun retry() {
        val link = local.value.link ?: return
        fetch(link)
    }

    private fun fetch(link: VideoLink) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            repository.release()
            local.update { current -> GrabLocalState(isFetching = true, feedback = current.feedback, link = link) }
            try {
                val details = repository.fetchDetails(link)
                local.update { current -> current.copy(isFetching = false, details = details, selected = QualityCatalog.preselect(preference = settings.preferredQuality(), qualities = details.qualities)) }
            } catch (exception: ExtractionException) {
                local.update { current -> current.copy(isFetching = false, error = exception.error) }
            }
        }
    }

    private fun completeShare() {
        viewModelScope.launch {
            if (repository.completeShare()) {
                local.update { current -> GrabLocalState(feedback = GrabFeedback.VideoDeleted, link = current.link) }
            }
        }
    }
}
