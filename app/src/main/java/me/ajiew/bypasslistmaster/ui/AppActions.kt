package me.ajiew.bypasslistmaster.ui

import me.ajiew.bypasslistmaster.domain.InstallEvent
import me.ajiew.bypasslistmaster.domain.MonitorMode
import me.ajiew.bypasslistmaster.domain.WhitelistFileFormat

data class AppActions(
    val onToggleService: (Boolean) -> Unit,
    val onImportWhitelist: () -> Unit,
    val onExportWhitelist: () -> Unit,
    val onCopyWhitelist: () -> Unit,
    val onPasteWhitelistFromClipboard: () -> Unit,
    val onAddToWhitelist: (String, String) -> Unit,
    val onIgnoreEvent: (String) -> Unit,
    val onDeleteEvent: (String) -> Unit,
    val onRestoreDeletedEvent: (InstallEvent) -> Unit,
    val onChangeMonitorMode: (MonitorMode) -> Unit,
    val onToggleAutoStart: (Boolean) -> Unit,
    val onChangeWhitelistFormat: (WhitelistFileFormat) -> Unit,
    val onStartRootDaemon: () -> Unit,
    val onStopRootDaemon: () -> Unit,
)
