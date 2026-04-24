package me.ajiew.bypasslistmaster.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import me.ajiew.bypasslistmaster.BypassListMasterApp

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }
        val app = context.applicationContext as BypassListMasterApp
        val shouldRestart = app.appContainer.repository.currentAutoStartEnabled()
        if (shouldRestart) {
            ContextCompat.startForegroundService(context, InstallMonitorService.createIntent(context))
        }
    }
}
