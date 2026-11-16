package com.example.grabclip.data.remote

import com.example.grabclip.domain.video.VideoQuality
import org.junit.Assert.assertEquals
import org.junit.Test

class FormatSelectorTest {
    @Test
    fun audioOnlyPrefersM4a() {
        assertEquals("ba[ext=m4a]/ba/b", FormatSelector.forQuality(VideoQuality(isAudioOnly = true, height = 0, resolution = 0, sizeBytes = 0L)))
    }

    @Test
    fun aKnownHeightCapsEveryAlternative() {
        val selector = FormatSelector.forQuality(VideoQuality(isAudioOnly = false, height = 720, resolution = 720, sizeBytes = 0L))

        assertEquals("bv*[height<=720][vcodec^=avc1]+ba[ext=m4a]/b[height<=720][ext=mp4]/bv*[height<=720]+ba/b[height<=720]/b", selector)
    }

    @Test
    fun anUnknownHeightAsksForTheBest() {
        assertEquals("bv*[vcodec^=avc1]+ba[ext=m4a]/b[ext=mp4]/bv*+ba/b", FormatSelector.forQuality(VideoQuality(isAudioOnly = false, height = 0, resolution = 0, sizeBytes = 0L)))
    }
}
