package com.ajiew.whitelistmaker.data

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import com.ajiew.whitelistmaker.domain.AppUiState
import com.ajiew.whitelistmaker.domain.EventDecision
import com.ajiew.whitelistmaker.domain.InstallEvent
import com.ajiew.whitelistmaker.domain.ServiceStatus
import com.ajiew.whitelistmaker.domain.UiMessage
import com.ajiew.whitelistmaker.domain.WhitelistFileFormat

class WhitelistRepository(
    private val application: Application,
    private val preferences: AppPreferences,
    private val installEventStore: InstallEventStore,
    private val whitelistFileStore: WhitelistFileStore,
) {
    private val messageFlow = MutableStateFlow<UiMessage?>(null)

    fun uiState(): Flow<AppUiState> {
        val settingsAndConfig = combine(
            preferences.settingsFlow(),
            preferences.whitelistConfigFlow(),
        ) { settings, whitelistConfig ->
            settings to whitelistConfig
        }
        val entriesAndEvents = combine(
            whitelistFileStore.entriesFlow(),
            installEventStore.eventsFlow(),
        ) { whitelistEntries, installEvents ->
            whitelistEntries to installEvents
        }
        return combine(
            settingsAndConfig,
            entriesAndEvents,
            messageFlow,
        ) { settingsPair, dataPair, message ->
            AppUiState(
                settings = settingsPair.first,
                whitelistConfig = settingsPair.second,
                whitelistEntries = dataPair.first,
                installEvents = dataPair.second,
                latestMessage = message,
            )
        }
    }

    fun updateAutoStart(enabled: Boolean) {
        preferences.updateAutoStart(enabled)
    }

    fun updateWhitelistFormat(format: WhitelistFileFormat) {
        preferences.updateWhitelistFormat(format)
        setMessage("已选择白名单文件格式：${format.displayName}")
    }

    fun updateServiceStatus(status: ServiceStatus) {
        preferences.updateServiceStatus(status)
    }

    fun resetRuntimeState() {
        preferences.resetRuntimeState()
    }

    fun currentAutoStartEnabled(): Boolean = preferences.currentSettings().autoStartOnBoot

    suspend fun recordInstalledPackage(packageName: String): InstallEvent {
        val label = resolveLabel(packageName)
        val event = installEventStore.recordInstallEvent(packageName, label)
        setMessage("发现新安装应用：$packageName")
        return event
    }

    suspend fun addEventToWhitelist(eventId: String, packageName: String) {
        val added = whitelistFileStore.appendPackage(packageName)
        installEventStore.updateDecision(
            eventId = eventId,
            decision = EventDecision.Added,
        )
        if (added) {
            setMessage("已将 $packageName 追加到白名单")
        } else {
            setMessage("$packageName 已存在于白名单中")
        }
    }

    suspend fun ignoreEvent(eventId: String) {
        installEventStore.updateDecision(eventId, EventDecision.Ignored)
        setMessage("已忽略该安装事件")
    }

    suspend fun deleteEvent(eventId: String): InstallEvent? {
        return installEventStore.deleteEvent(eventId)
    }

    suspend fun restoreDeletedEvent(event: InstallEvent) {
        installEventStore.restoreEvent(event)
        setMessage("已撤销删除")
    }

    suspend fun importWhitelist(uri: Uri): String {
        val displayName = whitelistFileStore.importWhitelist(uri)
        setMessage("已导入白名单文件：$displayName")
        return displayName
    }

    suspend fun exportWhitelist(uri: Uri) {
        whitelistFileStore.exportToUri(uri)
        setMessage("白名单已导出")
    }

    suspend fun copyWhitelistToClipboard() {
        val clipboardManager = application.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val content = whitelistFileStore.readExportText()
        clipboardManager.setPrimaryClip(ClipData.newPlainText("bypass whitelist", content))
        setMessage("白名单内容已复制到剪贴板")
    }

    suspend fun pasteWhitelistFromClipboard() {
        val clipboardManager = application.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val content = clipboardManager.primaryClip
            ?.takeIf { it.itemCount > 0 }
            ?.getItemAt(0)
            ?.coerceToText(application)
            ?.toString()
            .orEmpty()
        if (content.isBlank()) {
            setMessage("剪贴板中没有可用的白名单内容", isError = true)
            return
        }

        whitelistFileStore.replaceFromText(content)
            .onSuccess { count ->
                setMessage("已从剪贴板导入 $count 个白名单包名")
            }
            .onFailure { error ->
                setMessage(error.message ?: "剪贴板内容不符合当前白名单格式", isError = true)
            }
    }

    fun setMessage(text: String, isError: Boolean = false) {
        messageFlow.value = UiMessage(text = text, isError = isError)
    }

    private fun resolveLabel(packageName: String): String {
        return runCatching {
            val applicationInfo = application.packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            application.packageManager.getApplicationLabel(applicationInfo).toString()
        }.getOrDefault(packageName)
    }
}
