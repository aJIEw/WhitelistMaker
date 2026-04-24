package me.ajiew.bypasslistmaster.monitor.root

import me.ajiew.bypasslistmaster.domain.RootStatus

interface RootDaemonBridge {
    suspend fun getStatus(): RootStatus

    suspend fun startDaemon(): RootStatus

    suspend fun stopDaemon(): RootStatus
}
