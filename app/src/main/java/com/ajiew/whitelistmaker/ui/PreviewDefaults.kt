package com.ajiew.whitelistmaker.ui

import com.ajiew.whitelistmaker.domain.AppSettings
import com.ajiew.whitelistmaker.domain.AppUiState
import com.ajiew.whitelistmaker.domain.EventDecision
import com.ajiew.whitelistmaker.domain.EventDecisionOrigin
import com.ajiew.whitelistmaker.domain.InstallEvent
import com.ajiew.whitelistmaker.domain.ServiceStatus
import com.ajiew.whitelistmaker.domain.UiMessage
import com.ajiew.whitelistmaker.domain.WhitelistConfig
import com.ajiew.whitelistmaker.domain.WhitelistEntry

internal object PreviewDefaults {
    val actions = AppActions(
        onToggleService = { },
        onImportWhitelist = { },
        onExportWhitelist = { },
        onCopyWhitelist = { },
        onPasteWhitelistFromClipboard = { },
        onAddToWhitelist = { _, _ -> },
        onRemoveFromWhitelist = { },
        onIgnoreEvent = { },
        onRevertIgnoredEvent = { },
        onDeleteEventGroup = { },
        onRestoreDeletedEventGroup = { },
        onToggleAutoStart = { },
        onToggleRevert = { },
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
            WhitelistEntry(
                packageName = "com.android.chrome",
                appLabel = "Chrome",
            ),
            WhitelistEntry(
                packageName = "com.example.reader",
                appLabel = "",
            ),
            WhitelistEntry(
                packageName = "com.ajiew.whitelistmaker",
                appLabel = "Whitelist Maker",
            ),
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
                decisionOrigin = EventDecisionOrigin.Inherited,
            ),
            InstallEvent(
                id = "3",
                packageName = "com.example.music",
                appLabel = "Music",
                installedAt = 1_713_865_200_000,
                decision = EventDecision.Ignored,
            ),
            InstallEvent(
                id = "4",
                packageName = "com.example.music",
                appLabel = "Music",
                installedAt = 1_713_778_800_000,
                decision = EventDecision.Added,
            ),
            InstallEvent(
                id = "5",
                packageName = "com.example.music",
                appLabel = "Music",
                installedAt = 1_713_692_400_000,
                decision = EventDecision.Ignored,
            ),
        ),
        latestMessage = UiMessage("白名单文件已更新"),
    )
}
