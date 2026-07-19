package com.ajiew.whitelistmaker.monitor.standard

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ajiew.whitelistmaker.MainActivity
import com.ajiew.whitelistmaker.R
import com.ajiew.whitelistmaker.domain.EventDecision
import com.ajiew.whitelistmaker.domain.EventDecisionOrigin
import com.ajiew.whitelistmaker.domain.InstallEvent

class NotificationHelper(private val context: Context) {
    fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            SERVICE_CHANNEL_ID,
            "安装监听服务",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "用于展示安装监听服务的运行状态"
        }
        val installEventChannel = NotificationChannel(
            INSTALL_EVENT_CHANNEL_ID,
            "新安装应用",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "发现新安装应用时提醒你添加或忽略"
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannels(listOf(serviceChannel, installEventChannel))
    }

    fun buildForegroundNotification(contentText: String): Notification {
        val activityIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            1001,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(context, SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome).setContentTitle("Whitelist Maker")
            .setContentText(contentText).setContentIntent(pendingIntent).setOngoing(true)
            .setOnlyAlertOnce(true).build()
    }

    fun showInstallEventNotification(event: InstallEvent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Abort notification if no permission
            return
        }

        val notificationId = installEventNotificationId(event.id)
        val contentIntent = PendingIntent.getActivity(
            context,
            notificationId,
            Intent(context, MainActivity::class.java).putExtra("route", "home"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notificationBuilder = NotificationCompat.Builder(context, INSTALL_EVENT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome).setContentTitle(event.appLabel)
            .setContentText(
                if (event.decision == EventDecision.Added &&
                    event.decisionOrigin == EventDecisionOrigin.Inherited
                ) {
                    "${event.packageName} · 已在白名单，沿用上次结果"
                } else {
                    event.packageName
                },
            ).setContentIntent(contentIntent).setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)

        if (event.decision == EventDecision.Pending) {
            val addIntent = InstallEventActionReceiver.createIntent(
                context = context,
                action = InstallEventActionReceiver.ACTION_ADD_TO_WHITELIST,
                eventId = event.id,
                packageName = event.packageName,
                notificationId = notificationId,
            )
            val ignoreIntent = InstallEventActionReceiver.createIntent(
                context = context,
                action = InstallEventActionReceiver.ACTION_IGNORE_EVENT,
                eventId = event.id,
                packageName = event.packageName,
                notificationId = notificationId,
            )
            notificationBuilder.addAction(
                R.drawable.ic_launcher_monochrome,
                "添加",
                PendingIntent.getBroadcast(
                    context,
                    notificationId * ACTION_REQUEST_CODE_FACTOR + ADD_REQUEST_CODE_OFFSET,
                    addIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                ),
            ).addAction(
                R.drawable.ic_launcher_monochrome,
                "忽略",
                PendingIntent.getBroadcast(
                    context,
                    notificationId * ACTION_REQUEST_CODE_FACTOR + IGNORE_REQUEST_CODE_OFFSET,
                    ignoreIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                ),
            )
        }

        NotificationManagerCompat.from(context).notify(notificationId, notificationBuilder.build())
    }

    companion object {
        const val SERVICE_CHANNEL_ID = "install_monitor_channel"
        const val INSTALL_EVENT_CHANNEL_ID = "install_event_channel"
        const val NOTIFICATION_ID = 2001

        private const val ACTION_REQUEST_CODE_FACTOR = 10
        private const val ADD_REQUEST_CODE_OFFSET = 1
        private const val IGNORE_REQUEST_CODE_OFFSET = 2

        fun installEventNotificationId(eventId: String): Int {
            return 3000 + (eventId.hashCode() and Int.MAX_VALUE) % 100000
        }
    }
}
