package com.example.grabclip.app

import android.content.Context
import com.example.grabclip.data.local.SettingsPreferences
import com.example.grabclip.data.remote.VideoExtractor
import com.example.grabclip.data.repository.VideoRepository
import com.example.grabclip.data.storage.VideoFiles
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppGraph(context: Context) {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val settings: SettingsPreferences = SettingsPreferences(context.applicationContext)

    val videoExtractor: VideoExtractor = VideoExtractor(context = context.applicationContext, now = System::currentTimeMillis, settings = settings)

    private val videoFiles: VideoFiles = VideoFiles(context.applicationContext)

    val videoRepository: VideoRepository by lazy {
        VideoRepository(extractor = videoExtractor, files = videoFiles, onDownloadStarted = { DownloadService.start(context.applicationContext) }, scope = scope, settings = settings)
    }
}
