package com.example.grabclip.data.remote

private const val PERCENT_SCALE: Float = 100f
private const val PHASE_DROP: Float = 50f
private const val PRIMARY_SHARE: Float = 0.9f

class DownloadProgress {
    private var lastPercent: Float = 0f
    private var reported: Float = 0f

    private var phase: Int = 0

    fun advance(percent: Float): Float {
        if (percent < 0f) return reported
        if (percent < lastPercent - PHASE_DROP) {
            phase++
        }
        lastPercent = percent
        val share = (percent / PERCENT_SCALE).coerceIn(0f, 1f)
        val overall = if (phase == 0) share * PRIMARY_SHARE else PRIMARY_SHARE + share * (1f - PRIMARY_SHARE)
        reported = maxOf(reported, overall)
        return reported
    }
}
