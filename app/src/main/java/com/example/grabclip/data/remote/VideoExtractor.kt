package com.example.grabclip.data.remote

import android.content.Context
import com.example.grabclip.data.local.SettingsPreferences
import com.example.grabclip.domain.link.VideoLink
import com.example.grabclip.domain.video.QualityCatalog
import com.example.grabclip.domain.video.VideoDetails
import com.example.grabclip.domain.video.VideoError
import com.example.grabclip.domain.video.VideoQuality
import com.example.grabclip.domain.video.VideoStream
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.mapper.VideoFormat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.IOException

private const val ENGINE_REFRESH_MILLIS: Long = 86_400_000L

private const val AUDIO_FORMAT: String = "m4a"
private const val OUTPUT_TEMPLATE: String = "video.%(ext)s"
private const val PROCESS_ID: String = "grabclip-download"
private const val UPDATED_STATUS: String = "DONE"
private const val VIDEO_FORMAT: String = "mp4"

enum class EngineUpdate { CURRENT, UPDATED }

class VideoExtractor(private val now: () -> Long, private val context: Context, private val settings: SettingsPreferences) {
    private val engine: Mutex = Mutex()

    private var isInitialised: Boolean = false

    suspend fun download(url: String, directory: File, onProgress: (Float) -> Unit, quality: VideoQuality) {
        engine.withLock {
            initialise()
            val progress = DownloadProgress()
            val request = singleVideoRequest(url)
                .addOption("--no-mtime")
                .addOption("-f", FormatSelector.forQuality(quality))
                .addOption("-o", File(directory, OUTPUT_TEMPLATE).absolutePath)
            if (quality.isAudioOnly) {
                request.addOption("-x").addOption("--audio-format", AUDIO_FORMAT)
            } else {
                request.addOption("--merge-output-format", VIDEO_FORMAT)
            }
            try {
                engineCall { YoutubeDL.execute(request, PROCESS_ID) { percent, _, _ -> onProgress(progress.advance(percent)) } }
            } catch (exception: CancellationException) {
                YoutubeDL.destroyProcessById(PROCESS_ID)
                throw exception
            }
        }
    }

    suspend fun fetchDetails(link: VideoLink): VideoDetails = engine.withLock {
        initialise()
        val info = engineCall { YoutubeDL.getInfo(singleVideoRequest(link.url)) }
        VideoDetails(
            durationSeconds = info.duration,
            title = info.title ?: info.fulltitle.orEmpty(),
            qualities = QualityCatalog.build(info.formats.orEmpty().map(::toStream)),
            link = link,
        )
    }

    suspend fun prepare(): Boolean = engine.withLock {
        try {
            initialise()
            if (now() - settings.engineUpdatedAt() > ENGINE_REFRESH_MILLIS) {
                refresh()
            }
            true
        } catch (exception: ExtractionException) {
            false
        }
    }

    suspend fun update(): EngineUpdate = engine.withLock {
        initialise()
        refresh()
    }

    fun version(): String = YoutubeDL.versionName(context).orEmpty()

    private suspend fun initialise() {
        if (isInitialised) return
        engineCall {
            YoutubeDL.init(context)
            FFmpeg.init(context)
        }
        isInitialised = true
    }

    private suspend fun refresh(): EngineUpdate {
        val status = engineCall { YoutubeDL.updateYoutubeDL(context) }
        settings.setEngineUpdatedAt(now())
        return if (status?.name == UPDATED_STATUS) EngineUpdate.UPDATED else EngineUpdate.CURRENT
    }

    private suspend fun <T> engineCall(action: () -> T): T = try {
        runInterruptible(Dispatchers.IO) { action() }
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: YoutubeDL.CanceledException) {
        throw CancellationException(exception.message)
    } catch (exception: YoutubeDLException) {
        throw ExtractionException(ExtractorErrors.classify(exception.message))
    } catch (exception: IOException) {
        throw ExtractionException(VideoError.FAILED)
    } catch (exception: RuntimeException) {
        throw ExtractionException(VideoError.FAILED)
    }

    private fun singleVideoRequest(url: String): YoutubeDLRequest = YoutubeDLRequest(url).addOption("--no-playlist").addOption("--playlist-items", "1")

    private fun toStream(format: VideoFormat): VideoStream = VideoStream(
        height = format.height,
        width = format.width,
        sizeBytes = if (format.fileSize > 0L) format.fileSize else format.fileSizeApproximate,
        audioCodec = format.acodec.orEmpty(),
        container = format.ext.orEmpty(),
        videoCodec = format.vcodec.orEmpty(),
    )
}
