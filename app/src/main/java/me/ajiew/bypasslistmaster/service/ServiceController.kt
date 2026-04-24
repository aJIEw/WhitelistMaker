package me.ajiew.bypasslistmaster.service

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat

object ServiceController {
    private const val TAG = "ServiceController"

    fun start(context: Context) {
        Log.i(TAG, "onCreate: Start service")
        ContextCompat.startForegroundService(context, InstallMonitorService.createIntent(context))
    }

    fun stop(context: Context) {
        Log.i(TAG, "onCreate: Stop service")
        context.stopService(InstallMonitorService.createIntent(context))
    }
}
