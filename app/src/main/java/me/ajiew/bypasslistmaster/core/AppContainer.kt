package me.ajiew.bypasslistmaster.core

import android.app.Application
import me.ajiew.bypasslistmaster.data.AppPreferences
import me.ajiew.bypasslistmaster.data.BypassRepository
import me.ajiew.bypasslistmaster.data.InstallEventStore
import me.ajiew.bypasslistmaster.data.WhitelistFileStore
import me.ajiew.bypasslistmaster.monitor.root.RootDaemonBridge
import me.ajiew.bypasslistmaster.monitor.root.RootDaemonBridgeStub

class AppContainer(application: Application) {
    private val preferences = AppPreferences(application)
    private val installEventStore = InstallEventStore(application)
    private val whitelistFileStore = WhitelistFileStore(application, preferences)
    private val rootDaemonBridge: RootDaemonBridge = RootDaemonBridgeStub()

    val repository = BypassRepository(
        application = application,
        preferences = preferences,
        installEventStore = installEventStore,
        whitelistFileStore = whitelistFileStore,
        rootDaemonBridge = rootDaemonBridge,
    )
}
