package com.example.grabclip.ui.format

import org.junit.Assert.assertEquals
import org.junit.Test

class SizeFormatTest {
    private val units: SizeUnits = SizeUnits(gigabytes = "%1\$s GB", kilobytes = "%1\$s KB", megabytes = "%1\$s MB")

    @Test
    fun formatLeavesAnUnknownSizeBlank() {
        assertEquals("", SizeFormat.format(bytes = 0L, units = units))
        assertEquals("", SizeFormat.format(bytes = -1L, units = units))
    }

    @Test
    fun formatShowsGigabytesWithOneDecimal() {
        assertEquals("1.5 GB", SizeFormat.format(bytes = 1_610_612_736L, units = units))
    }

    @Test
    fun formatShowsKilobytesAndNeverZero() {
        assertEquals("1 KB", SizeFormat.format(bytes = 500L, units = units))
        assertEquals("2 KB", SizeFormat.format(bytes = 2_048L, units = units))
    }

    @Test
    fun formatShowsWholeMegabytes() {
        assertEquals("41 MB", SizeFormat.format(bytes = 42_991_616L, units = units))
    }
}
