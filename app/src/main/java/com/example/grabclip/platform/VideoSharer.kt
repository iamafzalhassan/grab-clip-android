package com.example.grabclip.platform

import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.net.toUri

class VideoSharer(private val context: Context, private val resultReceiver: Class<out BroadcastReceiver>) {
    fun share(mimeType: String, title: String, uri: String): Boolean {
        val stream = uri.toUri()
        val send = Intent(Intent.ACTION_SEND)
            .setType(mimeType)
            .putExtra(Intent.EXTRA_STREAM, stream)
            .putExtra(Intent.EXTRA_TITLE, title)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        send.clipData = ClipData.newRawUri(title, stream)
        return try {
            context.startActivity(Intent.createChooser(send, null, resultSender().intentSender))
            true
        } catch (exception: ActivityNotFoundException) {
            false
        }
    }

    private fun resultSender(): PendingIntent {
        val mutability = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
        return PendingIntent.getBroadcast(context, 0, Intent(context, resultReceiver), PendingIntent.FLAG_UPDATE_CURRENT or mutability)
    }
}
