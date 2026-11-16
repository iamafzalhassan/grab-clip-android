package com.example.grabclip.data.repository

import com.example.grabclip.data.local.SettingsPreferences
import com.example.grabclip.data.remote.ExtractionException
import com.example.grabclip.data.remote.VideoExtractor
import com.example.grabclip.data.storage.VideoFiles
import com.example.grabclip.domain.download.DownloadState
import com.example.grabclip.domain.download.DownloadedVideo
import com.example.grabclip.domain.link.VideoLink
import com.example.grabclip.domain.video.VideoDetails
import com.example.grabclip.domain.video.VideoError
import com.example.grabclip.domain.video.VideoQuality
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

private const val PROGRESS_STEP: Float = 0.005f

class VideoRepository(
    private val onDownloadStarted: () -> Unit,
    private val scope: CoroutineScope,
    private val settings: SettingsPreferences,
    private val extractor: VideoExtractor,
    private val files: VideoFiles,
) {
    private val state: MutableStateFlow<DownloadState> = MutableStateFlow(DownloadState.Idle)

    val download: StateFlow<DownloadState> = state

    @Volatile
    private var isShareTargetChosen: Boolean = false

    private var downloadJob: Job? = null

    fun acknowledgeFailure() = state.update { current -> if (current is DownloadState.Failed) DownloadState.Idle else current }

    fun cancelDownload() {
        downloadJob?.cancel()
    }

    suspend fun completeShare(): Boolean {
        val completed = state.value as? DownloadState.Completed
        if (!isShareTargetChosen || completed == null || completed.video.isKept) return false
        discard()
        return true
    }

    suspend fun discard() = withContext(Dispatchers.IO) {
        val video = (state.value as? DownloadState.Completed)?.video
        if (video != null && !video.isKept) {
            removeShared(video)
        }
        isShareTargetChosen = false
        state.value = DownloadState.Idle
    }

    suspend fun fetchDetails(link: VideoLink): VideoDetails = extractor.fetchDetails(link)

    fun onShareTargetChosen() {
        val video = (state.value as? DownloadState.Completed)?.video ?: return
        isShareTargetChosen = true
        if (!video.isKept) {
            settings.setPendingDelete(path = video.path, uri = video.uri)
        }
    }

    fun prepare() {
        scope.launch(Dispatchers.IO) {
            files.clearTemporary()
            files.deleteShared(path = settings.pendingDeletePath(), uri = settings.pendingDeleteUri())
            settings.clearPendingDelete()
            extractor.prepare()
        }
    }

    fun release() {
        if (state.value is DownloadState.Running) return
        isShareTargetChosen = false
        state.value = DownloadState.Idle
    }

    fun startDownload(details: VideoDetails, quality: VideoQuality) {
        if (state.value is DownloadState.Running) return
        state.value = DownloadState.Running(0f)
        isShareTargetChosen = false
        onDownloadStarted()
        downloadJob = scope.launch(Dispatchers.IO) { runDownload(details = details, quality = quality) }
    }

    private fun removeShared(video: DownloadedVideo) {
        files.deleteShared(path = video.path, uri = video.uri)
        settings.clearPendingDelete()
    }

    private suspend fun runDownload(details: VideoDetails, quality: VideoQuality) {
        try {
            val directory = files.newJobDirectory()
            extractor.download(directory = directory, onProgress = ::onProgress, quality = quality, url = details.link.url)
            state.value = DownloadState.Completed(files.finish(directory = directory, isKept = !settings.deleteAfterSharing(), quality = quality, title = details.title))
        } catch (exception: CancellationException) {
            fail(DownloadState.Idle)
            throw exception
        } catch (exception: ExtractionException) {
            fail(DownloadState.Failed(exception.error))
        } catch (exception: IOException) {
            fail(DownloadState.Failed(VideoError.FAILED))
        } catch (exception: RuntimeException) {
            fail(DownloadState.Failed(VideoError.FAILED))
        }
    }

    private fun onProgress(fraction: Float) = state.update { current -> if (current is DownloadState.Running && fraction - current.fraction >= PROGRESS_STEP) DownloadState.Running(fraction) else current }

    private fun fail(next: DownloadState) {
        files.clearTemporary()
        state.value = next
    }
}
