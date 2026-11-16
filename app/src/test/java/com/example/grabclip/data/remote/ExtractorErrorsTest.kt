package com.example.grabclip.data.remote

import com.example.grabclip.domain.video.VideoError
import org.junit.Assert.assertEquals
import org.junit.Test

class ExtractorErrorsTest {
    @Test
    fun classifyFallsBackToFailed() {
        assertEquals(VideoError.FAILED, ExtractorErrors.classify(null))
        assertEquals(VideoError.FAILED, ExtractorErrors.classify("ERROR: something nobody planned for"))
    }

    @Test
    fun classifyRecognisesAFullDisk() {
        assertEquals(VideoError.NO_SPACE, ExtractorErrors.classify("ERROR: unable to write data: [Errno 28] No space left on device"))
    }

    @Test
    fun classifyRecognisesALinkThatIsNotAVideo() {
        assertEquals(VideoError.UNSUPPORTED, ExtractorErrors.classify("ERROR: Unsupported URL: https://example.com/page"))
    }

    @Test
    fun classifyRecognisesAMissingConnection() {
        assertEquals(VideoError.OFFLINE, ExtractorErrors.classify("ERROR: Unable to download webpage: <urlopen error [Errno 7] No address associated with hostname>"))
        assertEquals(VideoError.OFFLINE, ExtractorErrors.classify("ERROR: [Errno -2] Name or service not known"))
    }

    @Test
    fun classifyRecognisesARemovedVideo() {
        assertEquals(VideoError.UNAVAILABLE, ExtractorErrors.classify("ERROR: [youtube] abc123: Video unavailable"))
    }

    @Test
    fun classifyRecognisesAVideoThatNeedsAnAccount() {
        assertEquals(VideoError.LOGIN_REQUIRED, ExtractorErrors.classify("ERROR: [Instagram] abc: Requested content is not available, rate-limit reached or login required"))
        assertEquals(VideoError.LOGIN_REQUIRED, ExtractorErrors.classify("ERROR: [youtube] abc: Sign in to confirm your age"))
    }
}
