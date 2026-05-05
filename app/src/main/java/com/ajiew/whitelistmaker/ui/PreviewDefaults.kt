package com.ajiew.whitelistmaker.ui

import com.ajiew.whitelistmaker.domain.AppSettings
import com.ajiew.whitelistmaker.domain.AppUiState
import com.ajiew.whitelistmaker.domain.EventDecision
import com.ajiew.whitelistmaker.domain.InstallEvent
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
        onToggleAutoStart = { },
        onChangeWhitelistFormat = { },
    )

    val uiState = AppUiState(
        settings = AppSettings(
            autoStartOnBoot = true,
            serviceStatus = ServiceStatus.Running,
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
            ),
            InstallEvent(
                id = "2",
                packageName = "com.example.music",
                appLabel = "Music",
                installedAt = 1_713_951_600_000,
                decision = EventDecision.Added,
            ),
        ),
        latestMessage = UiMessage("白名单文件已更新"),
    )
}
