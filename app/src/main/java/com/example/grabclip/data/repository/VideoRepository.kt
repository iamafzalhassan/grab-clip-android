package com.example.grabclip.data.repository

import com.example.grabclip.data.local.SettingsPreferences
import com.example.grabclip.data.remote.ExtractionException
import com.example.grabclip.data.remote.VideoExtractor
import com.example.grabclip.data.storage.VideoFiles
import com.example.grabclip.domain.download.DownloadState
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
        (state.value as? DownloadState.Completed)?.let { completed -> files.delete(completed.video) }
        isShareTargetChosen = false
        state.value = DownloadState.Idle
    }

    suspend fun fetchDetails(link: VideoLink): VideoDetails = extractor.fetchDetails(link)

    fun onShareTargetChosen() {
        isShareTargetChosen = true
    }

    fun prepare() {
        scope.launch(Dispatchers.IO) {
            files.clearPrivate()
            extractor.prepare()
        }
    }

    fun startDownload(details: VideoDetails, quality: VideoQuality) {
        if (state.value is DownloadState.Running) return
        state.value = DownloadState.Running(0f)
        isShareTargetChosen = false
        onDownloadStarted()
        downloadJob = scope.launch(Dispatchers.IO) { runDownload(details = details, quality = quality) }
    }

    private suspend fun runDownload(details: VideoDetails, quality: VideoQuality) {
        try {
            val directory = files.newJobDirectory()
            extractor.download(
                directory = directory,
                onProgress = { fraction -> state.update { current -> if (current is DownloadState.Running) DownloadState.Running(fraction) else current } },
                quality = quality,
                url = details.link.url,
            )
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

    private fun fail(next: DownloadState) {
        files.clearPrivate()
        state.value = next
    }
}
