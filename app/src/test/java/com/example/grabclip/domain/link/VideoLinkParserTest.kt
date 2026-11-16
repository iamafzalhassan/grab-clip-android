package com.example.grabclip.domain.link

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VideoLinkParserTest {
    @Test
    fun facebookVideoLinksAreRecognised() {
        assertEquals(VideoSource.FACEBOOK, VideoLinkParser.parse("https://fb.watch/abc123/")?.source)
        assertEquals(VideoSource.FACEBOOK, VideoLinkParser.parse("https://www.facebook.com/watch/?v=123456")?.source)
        assertEquals(VideoSource.FACEBOOK, VideoLinkParser.parse("https://www.facebook.com/share/v/1AbCdEf/")?.source)
        assertEquals(VideoSource.FACEBOOK, VideoLinkParser.parse("https://www.facebook.com/reel/987654321")?.source)
        assertEquals(VideoSource.FACEBOOK, VideoLinkParser.parse("https://m.facebook.com/story.php?story_fbid=1&id=2")?.source)
    }

    @Test
    fun instagramVideoLinksAreRecognised() {
        assertEquals(VideoSource.INSTAGRAM, VideoLinkParser.parse("https://www.instagram.com/reel/Cxyz123/")?.source)
        assertEquals(VideoSource.INSTAGRAM, VideoLinkParser.parse("https://www.instagram.com/p/Cxyz123/")?.source)
        assertEquals(VideoSource.INSTAGRAM, VideoLinkParser.parse("https://instagram.com/reels/Cxyz123")?.source)
    }

    @Test
    fun linkIsFoundInsideSurroundingText() {
        val link = VideoLinkParser.parse("Check this out https://www.instagram.com/reel/Cxyz123/?igsh=abc.")

        assertEquals(VideoLink(url = "https://www.instagram.com/reel/Cxyz123/?igsh=abc", source = VideoSource.INSTAGRAM), link)
    }

    @Test
    fun pagesThatAreNotVideosAreRejected() {
        assertNull(VideoLinkParser.parse("https://www.youtube.com/@somechannel"))
        assertNull(VideoLinkParser.parse("https://www.youtube.com/watch"))
        assertNull(VideoLinkParser.parse("https://www.instagram.com/someuser/"))
        assertNull(VideoLinkParser.parse("https://www.instagram.com/reels/"))
        assertNull(VideoLinkParser.parse("https://www.facebook.com/somepage"))
        assertNull(VideoLinkParser.parse("https://www.facebook.com/watch/"))
    }

    @Test
    fun textWithoutSupportedLinkIsRejected() {
        assertNull(VideoLinkParser.parse(null))
        assertNull(VideoLinkParser.parse(""))
        assertNull(VideoLinkParser.parse("just some copied words"))
        assertNull(VideoLinkParser.parse("https://vimeo.com/123456"))
    }

    @Test
    fun theFirstSupportedLinkWins() {
        val link = VideoLinkParser.parse("https://example.com/page https://youtu.be/first https://youtu.be/second")

        assertEquals("https://youtu.be/first", link?.url)
    }

    @Test
    fun youTubeVideoLinksAreRecognised() {
        assertEquals(VideoSource.YOUTUBE, VideoLinkParser.parse("https://www.youtube.com/watch?v=dQw4w9WgXcQ")?.source)
        assertEquals(VideoSource.YOUTUBE, VideoLinkParser.parse("https://youtu.be/dQw4w9WgXcQ?si=abc")?.source)
        assertEquals(VideoSource.YOUTUBE, VideoLinkParser.parse("https://m.youtube.com/shorts/abc123")?.source)
        assertEquals(VideoSource.YOUTUBE, VideoLinkParser.parse("https://music.youtube.com/watch?v=abc123&list=xyz")?.source)
        assertEquals(VideoSource.YOUTUBE, VideoLinkParser.parse("HTTPS://WWW.YOUTUBE.COM/watch?v=abc123")?.source)
    }
}
