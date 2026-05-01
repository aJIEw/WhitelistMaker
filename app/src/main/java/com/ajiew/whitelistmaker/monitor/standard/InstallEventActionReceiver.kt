package com.ajiew.whitelistmaker.monitor.standard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.ajiew.whitelistmaker.WhitelistMakerApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class InstallEventActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getStringExtra(EXTRA_EVENT_ID) ?: return
        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
        val pendingResult = goAsync()

        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate).launch {
            try {
                val repository = (context.applicationContext as WhitelistMakerApp).appContainer.repository
                when (intent.action) {
                    ACTION_ADD_TO_WHITELIST -> {
                        if (!packageName.isNullOrBlank()) {
                            repository.addEventToWhitelist(eventId, packageName)
                        }
                    }

                    ACTION_IGNORE_EVENT -> repository.ignoreEvent(eventId)
                }
                if (notificationId > 0) {
                    NotificationManagerCompat.from(context).cancel(notificationId)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_ADD_TO_WHITELIST = "com.ajiew.whitelistmaker.action.ADD_TO_WHITELIST"
        const val ACTION_IGNORE_EVENT = "com.ajiew.whitelistmaker.action.IGNORE_EVENT"

        private const val EXTRA_EVENT_ID = "event_id"
        private const val EXTRA_PACKAGE_NAME = "package_name"
        private const val EXTRA_NOTIFICATION_ID = "notification_id"

        fun createIntent(
            context: Context,
            action: String,
            eventId: String,
            packageName: String,
            notificationId: Int,
        ): Intent {
            return Intent(context, InstallEventActionReceiver::class.java)
                .setAction(action)
                .putExtra(EXTRA_EVENT_ID, eventId)
                .putExtra(EXTRA_PACKAGE_NAME, packageName)
                .putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
    }
}
