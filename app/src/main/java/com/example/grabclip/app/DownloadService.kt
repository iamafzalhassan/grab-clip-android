package com.example.grabclip.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.example.grabclip.GrabClipApplication
import com.example.grabclip.MainActivity
import com.example.grabclip.R
import com.example.grabclip.domain.download.DownloadState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class DownloadService : Service() {
    companion object {
        private const val NOTIFICATION_ID: Int = 1
        private const val PERCENT_SCALE: Int = 100

        private const val CHANNEL_ID: String = "downloads"

        fun start(context: Context) {
            try {
                ContextCompat.startForegroundService(context, Intent(context, DownloadService::class.java))
            } catch (exception: IllegalStateException) {
                return
            }
        }
    }

    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val notifications: NotificationManager by lazy { getSystemService(NotificationManager::class.java) }

    private var watchJob: Job? = null

    private fun watchDownload() {
        if (watchJob != null) return
        val repository = (application as GrabClipApplication).graph.videoRepository
        watchJob = scope.launch {
            repository.download
                .map { state -> (state as? DownloadState.Running)?.let { running -> (running.fraction * PERCENT_SCALE).toInt() } }
                .distinctUntilChanged()
                .collect { percent -> if (percent == null) finish() else notifications.notify(NOTIFICATION_ID, buildNotification(percent)) }
        }
    }

    private fun finish() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(percent: Int): Notification {
        val openApp = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setColor(ContextCompat.getColor(this, R.color.brand_ink))
            .setContentIntent(openApp)
            .setContentText(getString(R.string.progress_percent, percent))
            .setContentTitle(getString(R.string.notification_downloading_title))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(PERCENT_SCALE, percent, false)
            .setSilent(true)
            .setSmallIcon(R.drawable.ic_notification)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notifications.createNotificationChannel(NotificationChannel(CHANNEL_ID, getString(R.string.notification_channel_downloads), NotificationManager.IMPORTANCE_LOW))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC else 0
        ServiceCompat.startForeground(this, NOTIFICATION_ID, buildNotification(0), type)
        watchDownload()
        return START_NOT_STICKY
    }

    override fun onTimeout(startId: Int, fgsType: Int) {
        finish()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
