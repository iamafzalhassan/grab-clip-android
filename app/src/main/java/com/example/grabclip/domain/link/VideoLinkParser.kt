package com.example.grabclip.domain.link

import java.net.URI
import java.net.URISyntaxException

object VideoLinkParser {
    private val facebookMarkers: List<String> = listOf("/reel/", "/videos/", "/share/v/", "/share/r/", "/watch/")
    private val hostPrefixes: List<String> = listOf("www.", "m.", "mobile.", "web.", "music.")
    private val instagramMarkers: List<String> = listOf("/reel/", "/reels/", "/p/", "/tv/", "/share/")
    private val youTubeMarkers: List<String> = listOf("/shorts/", "/live/", "/embed/")

    private val urlPattern: Regex = Regex("""https?://[^\s<>"']+""", RegexOption.IGNORE_CASE)

    fun parse(text: String?): VideoLink? = text?.let { content -> urlPattern.findAll(content).firstNotNullOfOrNull { match -> toLink(match.value.trimEnd('.', ',', ';', ')', '!')) } }

    private fun toLink(url: String): VideoLink? {
        val uri = try {
            URI(url)
        } catch (exception: URISyntaxException) {
            return null
        }
        val host = stripPrefix(uri.host?.lowercase() ?: return null)
        val path = uri.rawPath.orEmpty()
        val query = uri.rawQuery.orEmpty()
        val source = when {
            host == "youtu.be" && path.length > 1 -> VideoSource.YOUTUBE
            host == "youtube.com" && isYouTubeVideo(path = path, query = query) -> VideoSource.YOUTUBE
            host == "instagram.com" && hasMarker(markers = instagramMarkers, path = path) -> VideoSource.INSTAGRAM
            host == "fb.watch" && path.length > 1 -> VideoSource.FACEBOOK
            host == "facebook.com" && isFacebookVideo(path = path, query = query) -> VideoSource.FACEBOOK
            else -> return null
        }
        return VideoLink(url = url, source = source)
    }

    private fun stripPrefix(host: String): String = hostPrefixes.firstOrNull(host::startsWith)?.let(host::removePrefix) ?: host

    private fun isYouTubeVideo(path: String, query: String): Boolean = (path == "/watch" && hasParameter(name = "v", query = query)) || hasMarker(markers = youTubeMarkers, path = path)

    private fun isFacebookVideo(path: String, query: String): Boolean {
        val isQueryPage = path == "/watch" || path == "/watch/" || path.endsWith(".php")
        return hasMarker(markers = facebookMarkers, path = path) || (isQueryPage && (hasParameter(name = "v", query = query) || hasParameter(name = "story_fbid", query = query)))
    }

    private fun hasMarker(path: String, markers: List<String>): Boolean = markers.any { marker -> path.substringAfter(marker, "").trim('/').isNotEmpty() }

    private fun hasParameter(name: String, query: String): Boolean = query.split('&').any { parameter -> parameter.startsWith("$name=") && parameter.length > name.length + 1 }
}
