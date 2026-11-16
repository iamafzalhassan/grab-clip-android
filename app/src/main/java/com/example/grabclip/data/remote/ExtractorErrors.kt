package com.example.grabclip.data.remote

import com.example.grabclip.domain.video.VideoError

class ExtractionException(val error: VideoError) : Exception(error.name)

object ExtractorErrors {
    private val loginMarkers: List<String> = listOf("login required", "log in", "sign in", "private", "cookies", "members-only", "age-restricted", "confirm your age")
    private val offlineMarkers: List<String> = listOf("name resolution", "getaddrinfo", "network is unreachable", "timed out", "connection refused", "no address associated", "failed to resolve", "urlopen error", "name or service not known", "network unreachable", "connection reset", "connection aborted")
    private val spaceMarkers: List<String> = listOf("no space left", "disk quota")
    private val unavailableMarkers: List<String> = listOf("unavailable", "not available", "removed", "does not exist", "deleted", "http error 404", "http error 410", "no video")
    private val unsupportedMarkers: List<String> = listOf("unsupported url", "is not a valid url", "no suitable extractor")

    fun classify(message: String?): VideoError {
        val text = message.orEmpty().lowercase()
        return when {
            matches(markers = spaceMarkers, text = text) -> VideoError.NO_SPACE
            matches(markers = offlineMarkers, text = text) -> VideoError.OFFLINE
            matches(markers = unsupportedMarkers, text = text) -> VideoError.UNSUPPORTED
            matches(markers = loginMarkers, text = text) -> VideoError.LOGIN_REQUIRED
            matches(markers = unavailableMarkers, text = text) -> VideoError.UNAVAILABLE
            else -> VideoError.FAILED
        }
    }

    private fun matches(text: String, markers: List<String>): Boolean = markers.any(text::contains)
}
