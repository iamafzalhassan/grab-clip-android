package com.example.grabclip.ui.format

import org.junit.Assert.assertEquals
import org.junit.Test

class DurationFormatTest {
    @Test
    fun formatShowsHoursOnlyWhenThereAreSome() {
        assertEquals("1:02:03", DurationFormat.format(3723))
        assertEquals("59:59", DurationFormat.format(3599))
    }

    @Test
    fun formatShowsMinutesAndPaddedSeconds() {
        assertEquals("0:00", DurationFormat.format(0))
        assertEquals("1:05", DurationFormat.format(65))
        assertEquals("12:48", DurationFormat.format(768))
    }

    @Test
    fun formatTreatsANegativeLengthAsZero() {
        assertEquals("0:00", DurationFormat.format(-5))
    }
}
