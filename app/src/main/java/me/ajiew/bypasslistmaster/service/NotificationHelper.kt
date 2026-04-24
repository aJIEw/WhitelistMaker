package me.ajiew.bypasslistmaster.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import me.ajiew.bypasslistmaster.MainActivity
import me.ajiew.bypasslistmaster.R

class NotificationHelper(private val context: Context) {
    fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "安装监听服务",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "用于展示安装监听服务的运行状态"
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    fun buildForegroundNotification(contentText: String): Notification {
        val activityIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            1001,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("BypassList Master")
            .setContentText(contentText)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "install_monitor_channel"
        const val NOTIFICATION_ID = 2001
    }
}
