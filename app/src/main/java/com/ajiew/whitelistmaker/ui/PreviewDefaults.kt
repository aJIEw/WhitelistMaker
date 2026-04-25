package com.ajiew.whitelistmaker.ui

import com.ajiew.whitelistmaker.domain.AppSettings
import com.ajiew.whitelistmaker.domain.AppUiState
import com.ajiew.whitelistmaker.domain.EventDecision
import com.ajiew.whitelistmaker.domain.InstallEvent
import com.ajiew.whitelistmaker.domain.MonitorMode
import com.ajiew.whitelistmaker.domain.RootAvailability
import com.ajiew.whitelistmaker.domain.RootStatus
import com.ajiew.whitelistmaker.domain.ServiceStatus
import com.ajiew.whitelistmaker.domain.UiMessage
import com.ajiew.whitelistmaker.domain.WhitelistConfig

internal object PreviewDefaults {
    val actions = AppActions(
        onToggleService = { },
        onImportWhitelist = { },
        onExportWhitelist = { },
        onCopyWhitelist = { },
        onPasteWhitelistFromClipboard = { },
        onAddToWhitelist = { _, _ -> },
        onIgnoreEvent = { },
        onDeleteEvent = { },
        onRestoreDeletedEvent = { },
        onChangeMonitorMode = { },
        onToggleAutoStart = { },
        onChangeWhitelistFormat = { },
        onStartRootDaemon = { },
        onStopRootDaemon = { },
    )

    val uiState = AppUiState(
        settings = AppSettings(
            monitorMode = MonitorMode.Standard,
            autoStartOnBoot = true,
            serviceStatus = ServiceStatus.Running,
        ),
        rootStatus = RootStatus(
            availability = RootAvailability.Unsupported,
            daemonRunning = false,
            message = "当前设备未接入 root daemon，普通模式可继续使用。",
        ),
        whitelistConfig = WhitelistConfig(
            importedUri = "preview://bypass-whitelist.txt",
            importedDisplayName = "bypass-whitelist.txt",
        ),
        whitelistEntries = listOf(
            "com.android.chrome",
            "com.example.reader",
            "com.ajiew.whitelistmaker",
        ),
        installEvents = listOf(
            InstallEvent(
                id = "1",
                packageName = "com.example.notes",
                appLabel = "Notes",
                installedAt = 1_713_955_200_000,
                sourceMode = MonitorMode.Standard,
            ),
            InstallEvent(
                id = "2",
                packageName = "com.example.music",
                appLabel = "Music",
                installedAt = 1_713_951_600_000,
                sourceMode = MonitorMode.Standard,
                decision = EventDecision.Added,
            ),
        ),
        latestMessage = UiMessage("白名单文件已更新"),
    )
}
