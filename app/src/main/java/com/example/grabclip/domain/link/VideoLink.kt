package com.example.grabclip.domain.link

import androidx.compose.runtime.Immutable

enum class VideoSource { FACEBOOK, INSTAGRAM, YOUTUBE }

@Immutable
data class VideoLink(val url: String, val source: VideoSource)
