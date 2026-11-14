package com.ajiew.whitelistmaker.demo

import com.ajiew.whitelistmaker.domain.AppSettings
import com.ajiew.whitelistmaker.domain.AppUiState
import com.ajiew.whitelistmaker.domain.EventDecision
import com.ajiew.whitelistmaker.domain.EventDecisionOrigin
import com.ajiew.whitelistmaker.domain.InstallEvent
import com.ajiew.whitelistmaker.domain.ServiceStatus
import com.ajiew.whitelistmaker.domain.WhitelistConfig
import com.ajiew.whitelistmaker.domain.WhitelistEntry
import com.ajiew.whitelistmaker.ui.AppActions
import java.time.Instant

internal object DemoData {
    val uiState = AppUiState(
        settings = AppSettings(
            autoStartOnBoot = true,
            serviceStatus = ServiceStatus.Running,
        ),
        whitelistConfig = WhitelistConfig(
            importedUri = "demo://bypass-whitelist.txt",
            importedDisplayName = "bypass-whitelist.txt",
        ),
        whitelistEntries = listOf(
            "com.android.calendar" to "日历",
            "com.android.calculator2" to "计算器",
            "com.android.camera2" to "相机",
            "com.android.contacts" to "联系人",
            "com.android.deskclock" to "时钟",
            "com.android.gallery3d" to "图库",
            "com.android.settings" to "设置",
            "com.android.dialer" to "电话",
            "com.android.messaging" to "短信",
            "com.android.email" to "电子邮件",
            "com.android.browser" to "浏览器",
            "com.android.storagemanager" to "文件管理器",
            "com.android.soundrecorder" to "录音机",
        ).map(::whitelistEntry),
        installEvents = listOf(
            installEvent(
                id = "demo-documentsui-20261112",
                packageName = "com.android.documentsui",
                appLabel = "文件",
                installedAt = timestamp("2026-11-12T06:42:00Z"),
            ),
            installEvent(
                id = "demo-calculator-20261112",
                packageName = "com.android.calculator2",
                appLabel = "计算器",
                installedAt = timestamp("2026-11-12T02:18:00Z"),
                decision = EventDecision.Added,
            ),
            installEvent(
                id = "demo-contacts-20261111",
                packageName = "com.android.contacts",
                appLabel = "联系人",
                installedAt = timestamp("2026-11-11T07:35:00Z"),
            ),
            installEvent(
                id = "demo-contacts-20261109",
                packageName = "com.android.contacts",
                appLabel = "联系人",
                installedAt = timestamp("2026-11-09T01:20:00Z"),
                decision = EventDecision.Added,
            ),
            installEvent(
                id = "demo-contacts-20261107",
                packageName = "com.android.contacts",
                appLabel = "联系人",
                installedAt = timestamp("2026-11-07T09:08:00Z"),
                decision = EventDecision.Ignored,
            ),
            installEvent(
                id = "demo-clock-20261110",
                packageName = "com.android.deskclock",
                appLabel = "时钟",
                installedAt = timestamp("2026-11-10T00:16:00Z"),
                decision = EventDecision.Ignored,
            ),
            installEvent(
                id = "demo-gallery-20261109",
                packageName = "com.android.gallery3d",
                appLabel = "图库",
                installedAt = timestamp("2026-11-09T04:50:00Z"),
                decision = EventDecision.Added,
                decisionOrigin = EventDecisionOrigin.Inherited,
            ),
            installEvent(
                id = "demo-gallery-20261108",
                packageName = "com.android.gallery3d",
                appLabel = "图库",
                installedAt = timestamp("2026-11-08T08:12:00Z"),
                decision = EventDecision.Ignored,
            ),
            installEvent(
                id = "demo-gallery-20261106",
                packageName = "com.android.gallery3d",
                appLabel = "图库",
                installedAt = timestamp("2026-11-06T03:28:00Z"),
                decision = EventDecision.Added,
            ),
            installEvent(
                id = "demo-gallery-20261104",
                packageName = "com.android.gallery3d",
                appLabel = "图库",
                installedAt = timestamp("2026-11-04T06:05:00Z"),
                decision = EventDecision.Added,
                decisionOrigin = EventDecisionOrigin.Inherited,
            ),
        ).sortedByDescending(InstallEvent::installedAt),
    )

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

    private fun whitelistEntry(entry: Pair<String, String>) = WhitelistEntry(
        packageName = entry.first,
        appLabel = entry.second,
    )

    private fun installEvent(
        id: String,
        packageName: String,
        appLabel: String,
        installedAt: Long,
        decision: EventDecision = EventDecision.Pending,
        decisionOrigin: EventDecisionOrigin = EventDecisionOrigin.Manual,
    ) = InstallEvent(
        id = id,
        packageName = packageName,
        appLabel = appLabel,
        installedAt = installedAt,
        decision = decision,
        decisionOrigin = decisionOrigin,
    )

    private fun timestamp(isoInstant: String): Long = Instant.parse(isoInstant).toEpochMilli()
}
