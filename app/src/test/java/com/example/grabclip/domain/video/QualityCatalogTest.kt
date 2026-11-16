package com.example.grabclip.domain.video

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QualityCatalogTest {
    private val audioOnly: VideoQuality = VideoQuality(isAudioOnly = true, height = 0, resolution = 0, sizeBytes = 5L)

    @Test
    fun buildAddsTheAudioToAVideoOnlyStream() {
        val qualities = QualityCatalog.build(listOf(videoOnly(codec = "avc1.640028", height = 1080, sizeBytes = 80L, width = 1920), audio(container = "m4a", sizeBytes = 5L), audio(container = "webm", sizeBytes = 6L)))

        assertEquals(listOf(VideoQuality(isAudioOnly = false, height = 1080, resolution = 1080, sizeBytes = 85L), audioOnly), qualities)
    }

    @Test
    fun buildFallsBackToBestAvailableWhenNoResolutionIsKnown() {
        val qualities = QualityCatalog.build(listOf(VideoStream(height = 0, width = 0, sizeBytes = 0L, audioCodec = "", container = "mp4", videoCodec = "")))

        assertEquals(listOf(VideoQuality(isAudioOnly = false, height = 0, resolution = 0, sizeBytes = 0L), VideoQuality(isAudioOnly = true, height = 0, resolution = 0, sizeBytes = 0L)), qualities)
    }

    @Test
    fun buildIgnoresStreamsWithNeitherVideoNorAudio() {
        val storyboard = VideoStream(height = 90, width = 160, sizeBytes = 0L, audioCodec = "none", container = "mhtml", videoCodec = "none")

        val qualities = QualityCatalog.build(listOf(storyboard, videoOnly(codec = "avc1.4d401f", height = 720, sizeBytes = 40L, width = 1280), audio(container = "m4a", sizeBytes = 5L)))

        assertEquals(listOf(VideoQuality(isAudioOnly = false, height = 720, resolution = 720, sizeBytes = 45L), audioOnly), qualities)
    }

    @Test
    fun buildKeepsAnUnknownSizeUnknown() {
        val qualities = QualityCatalog.build(listOf(videoOnly(codec = "avc1.4d401f", height = 720, sizeBytes = 0L, width = 1280), audio(container = "m4a", sizeBytes = 5L)))

        assertEquals(0L, qualities.first().sizeBytes)
    }

    @Test
    fun buildOffersOnly720And480() {
        val qualities = QualityCatalog.build(
            listOf(
                videoOnly(codec = "avc1.4d401e", height = 480, sizeBytes = 20L, width = 854),
                videoOnly(codec = "avc1.640028", height = 1080, sizeBytes = 80L, width = 1920),
                videoOnly(codec = "avc1.4d401f", height = 720, sizeBytes = 40L, width = 1280),
                videoOnly(codec = "avc1.4d4015", height = 360, sizeBytes = 10L, width = 640),
            ),
        )

        assertEquals(listOf(720, 480, 0), qualities.map(VideoQuality::resolution))
    }

    @Test
    fun buildOffersTheLowestWhenEverythingIsAbove720() {
        val qualities = QualityCatalog.build(listOf(videoOnly(codec = "avc1.640028", height = 1080, sizeBytes = 80L, width = 1920), videoOnly(codec = "avc1.640032", height = 1440, sizeBytes = 160L, width = 2560)))

        assertEquals(listOf(1080, 0), qualities.map(VideoQuality::resolution))
    }

    @Test
    fun buildOffersTheNearestLowerResolutionWhenOneIsMissing() {
        val qualities = QualityCatalog.build(listOf(videoOnly(codec = "avc1.4d401e", height = 540, sizeBytes = 25L, width = 960), videoOnly(codec = "avc1.4d4015", height = 360, sizeBytes = 10L, width = 640)))

        assertEquals(listOf(540, 360, 0), qualities.map(VideoQuality::resolution))
    }

    @Test
    fun buildNamesAVerticalVideoByItsShortSide() {
        val qualities = QualityCatalog.build(listOf(videoOnly(codec = "avc1.640028", height = 1920, sizeBytes = 30L, width = 1080)))

        assertEquals(VideoQuality(isAudioOnly = false, height = 1920, resolution = 1080, sizeBytes = 30L), qualities.first())
    }

    @Test
    fun buildSizesByTheH264StreamWhenOneExists() {
        val qualities = QualityCatalog.build(listOf(videoOnly(codec = "vp09.00.40.08", height = 1080, sizeBytes = 120L, width = 1920), videoOnly(codec = "avc1.640028", height = 1080, sizeBytes = 80L, width = 1920)))

        assertEquals(80L, qualities.first().sizeBytes)
    }

    @Test
    fun buildUsesAProgressiveStreamsOwnSize() {
        val progressive = VideoStream(height = 360, width = 640, sizeBytes = 12L, audioCodec = "mp4a.40.2", container = "mp4", videoCodec = "avc1.42001e")

        val qualities = QualityCatalog.build(listOf(progressive, audio(container = "m4a", sizeBytes = 5L)))

        assertEquals(12L, qualities.first().sizeBytes)
    }

    @Test
    fun preselectFallsBackToTheLowestWhenEverythingIsAbove() {
        assertEquals(720, QualityCatalog.preselect(preference = 360, qualities = ladder(1080, 720))?.resolution)
    }

    @Test
    fun preselectPicksAudioWhenAudioIsPreferred() {
        assertEquals(true, QualityCatalog.preselect(preference = QualityPreference.AUDIO_ONLY, qualities = ladder(1080, 720))?.isAudioOnly)
    }

    @Test
    fun preselectPicksTheHighestNotAboveThePreference() {
        assertEquals(720, QualityCatalog.preselect(preference = 720, qualities = ladder(1080, 720, 480))?.resolution)
        assertEquals(720, QualityCatalog.preselect(preference = 1080, qualities = ladder(720, 480))?.resolution)
    }

    @Test
    fun preselectReturnsNothingForAnEmptyList() {
        assertNull(QualityCatalog.preselect(preference = QualityPreference.DEFAULT, qualities = emptyList()))
    }

    private fun audio(sizeBytes: Long, container: String): VideoStream = VideoStream(height = 0, width = 0, sizeBytes = sizeBytes, audioCodec = "mp4a.40.2", container = container, videoCodec = "none")

    private fun ladder(vararg resolutions: Int): List<VideoQuality> = resolutions.map { resolution -> VideoQuality(isAudioOnly = false, height = resolution, resolution = resolution, sizeBytes = 0L) } + audioOnly

    private fun videoOnly(height: Int, width: Int, sizeBytes: Long, codec: String): VideoStream = VideoStream(height = height, width = width, sizeBytes = sizeBytes, audioCodec = "none", container = "mp4", videoCodec = codec)
}
