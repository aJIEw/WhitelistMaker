package com.ajiew.whitelistmaker.monitor.root

import com.ajiew.whitelistmaker.domain.RootStatus

interface RootDaemonBridge {
    suspend fun getStatus(): RootStatus

    suspend fun startDaemon(): RootStatus

    suspend fun stopDaemon(): RootStatus
}
