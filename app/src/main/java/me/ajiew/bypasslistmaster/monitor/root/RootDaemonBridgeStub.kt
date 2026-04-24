package me.ajiew.bypasslistmaster.monitor.root

import me.ajiew.bypasslistmaster.domain.RootAvailability
import me.ajiew.bypasslistmaster.domain.RootStatus

class RootDaemonBridgeStub : RootDaemonBridge {
    override suspend fun getStatus(): RootStatus = unavailableStatus()

    override suspend fun startDaemon(): RootStatus = unavailableStatus()

    override suspend fun stopDaemon(): RootStatus = unavailableStatus()

    private fun unavailableStatus(): RootStatus {
        return RootStatus(
            availability = RootAvailability.Unsupported,
            daemonRunning = false,
            message = "当前构建已预留 root daemon 接口，后续可接入 libsu 或原生守护进程。",
        )
    }
}
