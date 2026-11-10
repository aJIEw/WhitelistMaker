package com.ajiew.whitelistmaker.ui

import com.ajiew.whitelistmaker.domain.InstallEvent
import com.ajiew.whitelistmaker.domain.WhitelistFileFormat

data class AppActions(
    val onToggleService: (Boolean) -> Unit,
    val onImportWhitelist: () -> Unit,
    val onExportWhitelist: () -> Unit,
    val onCopyWhitelist: () -> Unit,
    val onPasteWhitelistFromClipboard: () -> Unit,
    val onAddToWhitelist: (String, String) -> Unit,
    val onRemoveFromWhitelist: (String) -> Unit,
    val onIgnoreEvent: (String) -> Unit,
    val onRevertIgnoredEvent: (String) -> Unit,
    val onDeleteEventGroup: (String) -> Unit,
    val onRestoreDeletedEventGroup: (List<InstallEvent>) -> Unit,
    val onToggleAutoStart: (Boolean) -> Unit,
    val onChangeWhitelistFormat: (WhitelistFileFormat) -> Unit,
    val onToggleRevert: (Boolean) -> Unit,
)
