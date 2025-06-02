package com.lin.tomato.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.lin.tomato.MainActivity
import com.lin.tomato.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TimerService : Service() {
    private val channelId = "tomato_timer_service_channel"
    private val notificationId = 1001
    private var timerJob: Job? = null
    private var serviceScope: CoroutineScope? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val duration = intent?.getIntExtra(EXTRA_DURATION, 0) ?: 0
        if (duration > 0) {
            startForeground(notificationId, buildNotification(duration))
            startTimer(duration)
        } else {
            stopSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        timerJob?.cancel()
        serviceScope?.cancel()
        super.onDestroy()
    }

    private fun startTimer(duration: Int) {
        serviceScope = CoroutineScope(Dispatchers.Default)
        timerJob = serviceScope?.launch {
            var remaining = duration
            while (remaining >= 0) {
                // sendUpdateBroadcast(remaining)
                updateNotification(remaining)
                delay(1000)
                remaining--
            }
            stopSelf()
        }
    }

    private fun sendUpdateBroadcast(remaining: Int) {
        val intent = Intent(ACTION_TIMER_UPDATE)
        intent.putExtra(EXTRA_REMAINING, remaining)
        sendBroadcast(intent)
    }

    private fun buildNotification(remaining: Int): Notification {
        val pendingIntent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Tomato Timer")
            .setContentText("Time left: ${formatTime(remaining)}")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(remaining: Int) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, buildNotification(remaining))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Timer Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return "%02d:%02d".format(minutes, remainingSeconds)
    }

    companion object {
        const val ACTION_TIMER_UPDATE = "com.lin.tomato.TIMER_UPDATE"
        const val EXTRA_REMAINING = "remaining"
        const val EXTRA_DURATION = "duration"
    }
}
