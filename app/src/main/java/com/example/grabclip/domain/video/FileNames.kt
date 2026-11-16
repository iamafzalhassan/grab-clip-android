package com.example.grabclip.domain.video

object FileNames {
    private const val MAX_LENGTH: Int = 60

    private const val FALLBACK_NAME: String = "video"

    private val spaces: Regex = Regex("""\s+""")
    private val unsafe: Regex = Regex("""[^\p{L}\p{M}\p{N} _-]+""")

    fun forTitle(extension: String, title: String): String {
        val name = title.replace(unsafe, " ").replace(spaces, " ").trim().take(MAX_LENGTH).trim().ifEmpty { FALLBACK_NAME }
        return "$name.$extension"
    }
}
