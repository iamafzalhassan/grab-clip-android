package com.example.grabclip

import android.app.Application
import com.example.grabclip.app.AppGraph

class GrabClipApplication : Application() {
    val graph: AppGraph by lazy { AppGraph(this) }

    override fun onCreate() {
        super.onCreate()
        graph.videoRepository.prepare()
    }
}
