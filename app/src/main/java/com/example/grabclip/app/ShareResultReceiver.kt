package com.example.grabclip.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.grabclip.GrabClipApplication

class ShareResultReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        (context.applicationContext as GrabClipApplication).graph.videoRepository.onShareTargetChosen()
    }
}
