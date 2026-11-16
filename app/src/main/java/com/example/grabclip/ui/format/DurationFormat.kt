package com.example.grabclip.ui.format

import java.util.Locale

private const val SECONDS_PER_HOUR: Int = 3600
private const val SECONDS_PER_MINUTE: Int = 60

object DurationFormat {
    fun format(seconds: Int): String {
        val total = seconds.coerceAtLeast(0)
        val hours = total / SECONDS_PER_HOUR
        val minutes = total % SECONDS_PER_HOUR / SECONDS_PER_MINUTE
        val remainder = total % SECONDS_PER_MINUTE
        return if (hours > 0) String.format(Locale.US, "%d:%02d:%02d", hours, minutes, remainder) else String.format(Locale.US, "%d:%02d", minutes, remainder)
    }
}
