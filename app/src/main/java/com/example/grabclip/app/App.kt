package com.example.grabclip.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.grabclip.feature.grab.GrabScreen
import com.example.grabclip.feature.grab.GrabViewModel
import com.example.grabclip.feature.settings.SettingsSheet
import com.example.grabclip.feature.settings.SettingsViewModel
import com.example.grabclip.platform.VideoSharer
import com.example.grabclip.ui.theme.GrabClipTheme

@Composable
fun App(graph: AppGraph) {
    val context = LocalContext.current
    val sharer = remember(context) { VideoSharer(context = context, resultReceiver = ShareResultReceiver::class.java) }
    var isSettingsOpen by rememberSaveable { mutableStateOf(false) }

    GrabClipTheme {
        GrabScreen(
            onOpenSettings = { isSettingsOpen = true },
            onShare = { video -> sharer.share(mimeType = video.mimeType, title = video.title, uri = video.uri) },
            viewModel = viewModel { GrabViewModel(repository = graph.videoRepository, settings = graph.settings) },
        )
        if (isSettingsOpen) {
            SettingsSheet(onDismiss = { isSettingsOpen = false }, viewModel = viewModel { SettingsViewModel(extractor = graph.videoExtractor, settings = graph.settings) })
        }
    }
}
