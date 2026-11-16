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
import com.example.grabclip.domain.download.DownloadedVideo
import com.example.grabclip.domain.video.FileNames
import com.example.grabclip.domain.video.VideoQuality
import java.io.File
import java.io.IOException
import java.util.UUID

private const val AUDIO_PREFIX: String = "audio/"
private const val AUTHORITY_SUFFIX: String = ".files"
private const val DOWNLOADS_FOLDER: String = "downloads"
private const val FALLBACK_MIME_TYPE: String = "video/*"
private const val GALLERY_FOLDER: String = "GrabClip"

private val knownMimeTypes: Map<String, String> = mapOf("m4a" to "audio/mp4", "m4v" to "video/mp4", "mkv" to "video/x-matroska", "mp3" to "audio/mpeg", "mp4" to "video/mp4", "webm" to "video/webm")

private val partialExtensions: Set<String> = setOf("part", "tmp", "ytdl")

class VideoFiles(private val context: Context) {
    private val downloadsRoot: File = File(context.cacheDir, DOWNLOADS_FOLDER)

    fun clearPrivate() {
        downloadsRoot.deleteRecursively()
    }

    fun delete(video: DownloadedVideo) {
        if (!video.isKept && video.path.isNotEmpty()) {
            File(video.path).parentFile?.deleteRecursively()
        }
    }

    fun finish(isKept: Boolean, title: String, directory: File, quality: VideoQuality): DownloadedVideo {
        val output = directory.listFiles().orEmpty().filter { file -> file.isFile && file.extension.lowercase() !in partialExtensions }.maxByOrNull(File::length) ?: throw IOException("the download left no file")
        val named = File(directory, FileNames.forTitle(extension = output.extension, title = title))
        val file = if (output.renameTo(named)) named else output
        val mimeType = mimeTypeOf(file.extension)
        val sizeBytes = file.length()
        if (!isKept) {
            return DownloadedVideo(isKept = false, sizeBytes = sizeBytes, mimeType = mimeType, path = file.absolutePath, title = title, uri = sharedUri(file).toString(), quality = quality)
        }
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) insertIntoGallery(mimeType = mimeType, file = file) else copyToAppFolder(mimeType = mimeType, file = file)
        directory.deleteRecursively()
        return DownloadedVideo(isKept = true, sizeBytes = sizeBytes, mimeType = mimeType, path = "", title = title, uri = uri.toString(), quality = quality)
    }

    fun newJobDirectory(): File {
        clearPrivate()
        return File(downloadsRoot, UUID.randomUUID().toString()).apply { mkdirs() }
    }

    private fun mimeTypeOf(extension: String): String {
        val key = extension.lowercase()
        return knownMimeTypes[key] ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(key) ?: FALLBACK_MIME_TYPE
    }

    private fun sharedUri(file: File): Uri = FileProvider.getUriForFile(context, context.packageName + AUTHORITY_SUFFIX, file)

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun insertIntoGallery(mimeType: String, file: File): Uri {
        val isAudio = mimeType.startsWith(AUDIO_PREFIX)
        val collection = if (isAudio) MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY) else MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val folder = (if (isAudio) Environment.DIRECTORY_MUSIC else Environment.DIRECTORY_MOVIES) + File.separator + GALLERY_FOLDER
        val resolver = context.contentResolver
        val pending = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, folder)
        }
        val uri = resolver.insert(collection, pending) ?: throw IOException("the gallery refused the file")
        try {
            val output = resolver.openOutputStream(uri) ?: throw IOException("the gallery entry could not be opened")
            output.use { target -> file.inputStream().use { source -> source.copyTo(target) } }
        } catch (exception: IOException) {
            resolver.delete(uri, null, null)
            throw exception
        }
        resolver.update(uri, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
        return uri
    }

    private fun copyToAppFolder(mimeType: String, file: File): Uri {
        val type = if (mimeType.startsWith(AUDIO_PREFIX)) Environment.DIRECTORY_MUSIC else Environment.DIRECTORY_MOVIES
        val folder = context.getExternalFilesDir(type) ?: throw IOException("shared storage is not available")
        return sharedUri(file.copyTo(overwrite = true, target = File(folder, file.name)))
    }
}
