package com.example.grabclip.platform

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

private const val TEXT_MIME_TYPES: String = "text/*"

class ClipboardReader(private val context: Context) {
    companion object {
        private var lastSeenTimestamp: Long = -1L
    }

    private val clipboard: ClipboardManager? = context.getSystemService(ClipboardManager::class.java)

    fun read(): String? {
        val manager = clipboard ?: return null
        manager.primaryClipDescription?.let { description -> lastSeenTimestamp = description.timestamp }
        return try {
            manager.primaryClip?.takeIf { clip -> clip.itemCount > 0 }?.getItemAt(0)?.coerceToText(context)?.toString()
        } catch (exception: SecurityException) {
            null
        }
    }

    fun readIfChanged(): String? {
        val description = clipboard?.primaryClipDescription ?: return null
        if (description.timestamp == lastSeenTimestamp) return null
        if (!description.hasMimeType(TEXT_MIME_TYPES)) {
            lastSeenTimestamp = description.timestamp
            return null
        }
        return read()
    }
}

@Composable
fun rememberClipboardReader(): ClipboardReader {
    val context = LocalContext.current
    return remember(context) { ClipboardReader(context.applicationContext) }
}
