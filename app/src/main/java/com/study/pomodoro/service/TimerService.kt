package com.study.pomodoro.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.study.pomodoro.R

class TimerService : Service() {

    companion object {
        const val CHANNEL_ID = "pomodoro_timer"
        const val NOTIF_ID = 1
        const val EXTRA_SUBJECT = "subject"
        const val EXTRA_TIME = "time"
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val subject = intent?.getStringExtra(EXTRA_SUBJECT) ?: "Study"
        val time = intent?.getStringExtra(EXTRA_TIME) ?: ""
        startForeground(NOTIF_ID, buildNotification(subject, time))
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        val mgr = getSystemService(NotificationManager::class.java)
        if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
            mgr.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Pomodoro Timer", NotificationManager.IMPORTANCE_LOW)
            )
        }
    }

    private fun buildNotification(subject: String, time: String): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer_notif)
            .setContentTitle(subject)
            .setContentText(time)
            .setOngoing(true)
            .setSilent(true)
            .build()
}
