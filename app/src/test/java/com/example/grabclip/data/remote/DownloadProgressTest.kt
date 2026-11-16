package com.example.grabclip.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

private const val TOLERANCE: Float = 0.0001f

class DownloadProgressTest {
    @Test
    fun aSecondStreamFillsTheLastTenth() {
        val progress = DownloadProgress()
        progress.advance(100f)

        assertEquals(0.91f, progress.advance(10f), TOLERANCE)
        assertEquals(1f, progress.advance(100f), TOLERANCE)
    }

    @Test
    fun anUnknownPercentKeepsTheLastValue() {
        val progress = DownloadProgress()
        progress.advance(50f)

        assertEquals(0.45f, progress.advance(-1f), TOLERANCE)
    }

    @Test
    fun progressNeverMovesBackwards() {
        val progress = DownloadProgress()
        progress.advance(50f)

        assertEquals(0.45f, progress.advance(40f), TOLERANCE)
    }

    @Test
    fun theFirstStreamFillsNineTenths() {
        val progress = DownloadProgress()

        assertEquals(0.45f, progress.advance(50f), TOLERANCE)
        assertEquals(0.9f, progress.advance(100f), TOLERANCE)
    }
}
