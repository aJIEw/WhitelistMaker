package me.ajiew.bypasslistmaster.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import me.ajiew.bypasslistmaster.BypassListMasterApp
import me.ajiew.bypasslistmaster.domain.MonitorMode
import me.ajiew.bypasslistmaster.domain.ServiceStatus

class InstallMonitorService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var packageReceiver: BroadcastReceiver
    private lateinit var notificationHelper: NotificationHelper

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate: service is being created")
        notificationHelper = NotificationHelper(this)
        notificationHelper.createNotificationChannel()
        startForeground(
            NotificationHelper.NOTIFICATION_ID,
            notificationHelper.buildForegroundNotification("正在监听新安装的应用"),
        )
        Log.i(TAG, "onCreate: foreground notification started")
        app().appContainer.repository.updateServiceStatus(ServiceStatus.Starting)
        registerInstallReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(
            TAG,
            "onStartCommand: flags=$flags startId=$startId mode=${app().appContainer.repository.currentMonitorMode()}",
        )
        val repository = app().appContainer.repository
        serviceScope.launch {
            if (repository.currentMonitorMode() == MonitorMode.EnhancedRoot) {
                Log.i(TAG, "onStartCommand: requesting root daemon startup")
                val rootStatus = repository.startRootDaemon()
                Log.i(TAG, "onStartCommand: root daemon status=${rootStatus.message}")
                repository.setMessage(rootStatus.message)
            }
            repository.updateServiceStatus(ServiceStatus.Running)
            repository.setMessage("监听服务已启动，正在等待新安装应用。")
            Log.i(TAG, "onStartCommand: service status updated to running")
        }
        return START_STICKY
    }

    override fun onDestroy() {
        Log.i(TAG, "onDestroy: service is being destroyed, unregistering receiver")
        unregisterReceiver(packageReceiver)
        app().appContainer.repository.apply {
            updateServiceStatus(ServiceStatus.Stopped)
            setMessage("监听服务已停止。")
        }
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun registerInstallReceiver() {
        val repository = app().appContainer.repository
        packageReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.i(
                    TAG,
                    "onReceive: action=${intent.action} data=${intent.data} replacing=${intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)}",
                )
                if (intent.action != Intent.ACTION_PACKAGE_ADDED) {
                    Log.d(TAG, "onReceive: ignored non-package-added action")
                    return
                }
                if (intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
                    Log.i(TAG, "onReceive: ignored package replacement event")
                    return
                }
                val packageName = intent.data?.schemeSpecificPart ?: return
                Log.i(TAG, "onReceive: package added detected for $packageName")
                serviceScope.launch {
                    Log.i(TAG, "onReceive: persisting install event for $packageName")
                    repository.recordInstalledPackage(
                        packageName = packageName,
                        sourceMode = currentMonitorMode(),
                    )
                    Log.i(TAG, "onReceive: install event persisted for $packageName")
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_PACKAGE_ADDED).apply {
            addDataScheme("package")
        }
        Log.i(
            TAG,
            "registerInstallReceiver: registering ACTION_PACKAGE_ADDED receiver with exported=${Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU}",
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(packageReceiver, filter, RECEIVER_EXPORTED)
        } else {
            ContextCompat.registerReceiver(this, packageReceiver, filter, ContextCompat.RECEIVER_EXPORTED)
        }
        Log.i(TAG, "registerInstallReceiver: receiver registered")
    }

    private fun currentMonitorMode(): MonitorMode {
        return app().appContainer.repository.currentMonitorMode()
    }

    private fun app(): BypassListMasterApp = application as BypassListMasterApp

    companion object {
        private const val TAG = "InstallMonitorService"

        fun createIntent(context: Context): Intent = Intent(context, InstallMonitorService::class.java)
    }
}
