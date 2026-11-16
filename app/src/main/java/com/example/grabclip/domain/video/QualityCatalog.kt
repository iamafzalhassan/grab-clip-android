package com.example.grabclip.domain.video

object QualityPreference {
    const val AUDIO_ONLY: Int = -1
    const val DEFAULT: Int = 720

    val options: List<Int> = listOf(2160, 1440, 1080, 720, 480, 360, AUDIO_ONLY)
}

object QualityCatalog {
    private const val PREFERRED_AUDIO_CONTAINER: String = "m4a"
    private const val PREFERRED_VIDEO_CODEC: String = "avc1"

    fun build(streams: List<VideoStream>): List<VideoQuality> {
        val audioBytes = audioSize(streams)
        val videos = streams
            .filter { stream -> stream.hasVideo && stream.resolution > 0 }
            .groupBy(VideoStream::resolution)
            .map { (resolution, group) -> VideoQuality(isAudioOnly = false, height = group.maxOf(VideoStream::height), resolution = resolution, sizeBytes = videoSize(audioBytes = audioBytes, group = group)) }
            .sortedByDescending(VideoQuality::resolution)
        val best = VideoQuality(isAudioOnly = false, height = 0, resolution = 0, sizeBytes = 0L)
        return videos.ifEmpty { listOf(best) } + VideoQuality(isAudioOnly = true, height = 0, resolution = 0, sizeBytes = audioBytes)
    }

    fun preselect(preference: Int, qualities: List<VideoQuality>): VideoQuality? {
        val videos = qualities.filterNot(VideoQuality::isAudioOnly)
        val preferred = when (preference) {
            QualityPreference.AUDIO_ONLY -> qualities.firstOrNull(VideoQuality::isAudioOnly)
            else -> videos.firstOrNull { quality -> quality.resolution in 1..preference } ?: videos.lastOrNull()
        }
        return preferred ?: qualities.firstOrNull()
    }

    private fun audioSize(streams: List<VideoStream>): Long {
        val audioOnly = streams.filter { stream -> stream.hasAudio && !stream.hasVideo }
        return audioOnly.filter { stream -> stream.container == PREFERRED_AUDIO_CONTAINER }.ifEmpty { audioOnly }.maxOfOrNull(VideoStream::sizeBytes) ?: 0L
    }

    private fun videoSize(audioBytes: Long, group: List<VideoStream>): Long {
        val stream = group.filter { candidate -> candidate.videoCodec.startsWith(PREFERRED_VIDEO_CODEC) }.ifEmpty { group }.maxByOrNull(VideoStream::sizeBytes) ?: return 0L
        return when {
            stream.sizeBytes <= 0L -> 0L
            stream.hasAudio -> stream.sizeBytes
            else -> stream.sizeBytes + audioBytes
        }
    }
}
