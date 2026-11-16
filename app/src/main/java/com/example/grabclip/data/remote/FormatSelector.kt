package com.example.grabclip.data.remote

import com.example.grabclip.domain.video.VideoQuality

object FormatSelector {
    private const val AUDIO: String = "ba[ext=m4a]/ba/b"
    private const val BEST: String = "bv*[vcodec^=avc1]+ba[ext=m4a]/b[ext=mp4]/bv*+ba/b"

    fun forQuality(quality: VideoQuality): String = when {
        quality.isAudioOnly -> AUDIO
        quality.height <= 0 -> BEST
        else -> capped(quality.height)
    }

    private fun capped(height: Int): String = "bv*[height<=$height][vcodec^=avc1]+ba[ext=m4a]/b[height<=$height][ext=mp4]/bv*[height<=$height]+ba/b[height<=$height]/b"
}
