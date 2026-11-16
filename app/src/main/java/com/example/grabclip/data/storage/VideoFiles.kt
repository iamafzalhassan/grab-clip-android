package com.example.grabclip.data.storage

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.example.grabclip.domain.download.DownloadedVideo
import com.example.grabclip.domain.video.FileNames
import com.example.grabclip.domain.video.VideoQuality
import java.io.File
import java.io.IOException
import java.util.UUID

private const val AUTHORITY_SUFFIX: String = ".files"
private const val FALLBACK_MIME_TYPE: String = "video/*"
private const val SHARED_FOLDER: String = "GrabClip"
private const val TEMPORARY_FOLDER: String = "downloads"

private val knownMimeTypes: Map<String, String> = mapOf("m4a" to "audio/mp4", "m4v" to "video/mp4", "mkv" to "video/x-matroska", "mp3" to "audio/mpeg", "mp4" to "video/mp4", "webm" to "video/webm")

private val partialExtensions: Set<String> = setOf("part", "tmp", "ytdl")

class VideoFiles(private val context: Context) {
    private val temporaryRoot: File = File(context.cacheDir, TEMPORARY_FOLDER)

    fun clearTemporary() {
        temporaryRoot.deleteRecursively()
    }

    fun deleteShared(path: String, uri: String) {
        try {
            if (path.isNotEmpty()) {
                File(path).delete()
            } else if (uri.isNotEmpty()) {
                context.contentResolver.delete(uri.toUri(), null, null)
            }
        } catch (exception: RuntimeException) {
            return
        }
    }

    fun finish(isKept: Boolean, title: String, directory: File, quality: VideoQuality): DownloadedVideo {
        val output = directory.listFiles().orEmpty().filter { file -> file.isFile && file.extension.lowercase() !in partialExtensions }.maxByOrNull(File::length) ?: throw IOException("the download left no file")
        val named = File(directory, FileNames.forTitle(extension = output.extension, title = title))
        val file = if (output.renameTo(named)) named else output
        val mimeType = mimeTypeOf(file.extension)
        val sizeBytes = file.length()
        val path: String
        val uri: Uri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            path = ""
            uri = insertIntoDownloads(mimeType = mimeType, file = file)
        } else {
            val copy = copyToAppDownloads(file)
            path = copy.absolutePath
            uri = FileProvider.getUriForFile(context, context.packageName + AUTHORITY_SUFFIX, copy)
        }
        directory.deleteRecursively()
        return DownloadedVideo(isKept = isKept, sizeBytes = sizeBytes, mimeType = mimeType, path = path, title = title, uri = uri.toString(), quality = quality)
    }

    fun newJobDirectory(): File {
        clearTemporary()
        return File(temporaryRoot, UUID.randomUUID().toString()).apply { mkdirs() }
    }

    private fun mimeTypeOf(extension: String): String {
        val key = extension.lowercase()
        return knownMimeTypes[key] ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(key) ?: FALLBACK_MIME_TYPE
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun insertIntoDownloads(mimeType: String, file: File): Uri {
        val resolver = context.contentResolver
        val pending = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + File.separator + SHARED_FOLDER)
        }
        val uri = resolver.insert(MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), pending) ?: throw IOException("the Downloads folder refused the file")
        try {
            val output = resolver.openOutputStream(uri) ?: throw IOException("the Downloads entry could not be opened")
            output.use { target -> file.inputStream().use { source -> source.copyTo(target) } }
        } catch (exception: IOException) {
            resolver.delete(uri, null, null)
            throw exception
        }
        resolver.update(uri, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
        return uri
    }

    private fun copyToAppDownloads(file: File): File {
        val folder = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: throw IOException("shared storage is not available")
        return file.copyTo(overwrite = true, target = File(folder, file.name))
    }
}
