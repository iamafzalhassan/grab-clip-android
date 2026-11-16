package com.example.grabclip.domain.video

import org.junit.Assert.assertEquals
import org.junit.Test

class FileNamesTest {
    @Test
    fun forTitleFallsBackWhenNothingUsableIsLeft() {
        assertEquals("video.mp4", FileNames.forTitle(extension = "mp4", title = ""))
        assertEquals("video.mp4", FileNames.forTitle(extension = "mp4", title = "?!/\\"))
    }

    @Test
    fun forTitleKeepsLettersFromOtherScripts() {
        assertEquals("Café Zürich.m4a", FileNames.forTitle(extension = "m4a", title = "Café Zürich"))
    }

    @Test
    fun forTitleRemovesCharactersAFileNameCannotHold() {
        assertEquals("How to Build a Deck in 1 Weekend.mp4", FileNames.forTitle(extension = "mp4", title = "How to: Build a Deck / in 1 Weekend?!"))
    }

    @Test
    fun forTitleShortensALongTitle() {
        val name = FileNames.forTitle(extension = "mp4", title = "a".repeat(200))

        assertEquals("a".repeat(60) + ".mp4", name)
    }
}
