package me.ajiew.bypasslistmaster.data

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import me.ajiew.bypasslistmaster.domain.AppUiState
import me.ajiew.bypasslistmaster.domain.EventDecision
import me.ajiew.bypasslistmaster.domain.InstallEvent
import me.ajiew.bypasslistmaster.domain.MonitorMode
import me.ajiew.bypasslistmaster.domain.RootStatus
import me.ajiew.bypasslistmaster.domain.ServiceStatus
import me.ajiew.bypasslistmaster.domain.UiMessage
import me.ajiew.bypasslistmaster.monitor.root.RootDaemonBridge

class BypassRepository(
    private val application: Application,
    private val preferences: AppPreferences,
    private val installEventStore: InstallEventStore,
    private val whitelistFileStore: WhitelistFileStore,
    private val rootDaemonBridge: RootDaemonBridge,
) {
    private val messageFlow = MutableStateFlow<UiMessage?>(null)

    fun uiState(rootStatus: Flow<RootStatus>): Flow<AppUiState> {
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
            rootStatus,
            messageFlow,
        ) { settingsPair, dataPair, currentRootStatus, message ->
            AppUiState(
                settings = settingsPair.first,
                whitelistConfig = settingsPair.second,
                whitelistEntries = dataPair.first,
                installEvents = dataPair.second,
                rootStatus = currentRootStatus,
                latestMessage = message,
            )
        }
    }

    suspend fun refreshRootStatus(): RootStatus = rootDaemonBridge.getStatus()

    suspend fun startRootDaemon(): RootStatus = rootDaemonBridge.startDaemon()

    suspend fun stopRootDaemon(): RootStatus = rootDaemonBridge.stopDaemon()

    fun updateMonitorMode(mode: MonitorMode) {
        preferences.updateMonitorMode(mode)
        if (mode == MonitorMode.EnhancedRoot) {
            setMessage("已切换到增强模式，当前构建会优先展示 root 状态并保留标准监听作为兜底。")
        }
    }

    fun updateAutoStart(enabled: Boolean) {
        preferences.updateAutoStart(enabled)
    }

    fun updateServiceStatus(status: ServiceStatus) {
        preferences.updateServiceStatus(status)
    }

    fun resetRuntimeState() {
        preferences.resetRuntimeState()
    }

    fun currentMonitorMode(): MonitorMode = preferences.currentSettings().monitorMode

    fun currentAutoStartEnabled(): Boolean = preferences.currentSettings().autoStartOnBoot

    suspend fun recordInstalledPackage(packageName: String, sourceMode: MonitorMode) {
        val label = resolveLabel(packageName)
        installEventStore.recordInstallEvent(packageName, label, sourceMode)
        setMessage("发现新安装应用：$packageName")
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
