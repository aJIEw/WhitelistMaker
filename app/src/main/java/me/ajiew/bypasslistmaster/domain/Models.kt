package me.ajiew.bypasslistmaster.domain

enum class MonitorMode {
    Standard,
    EnhancedRoot,
}

enum class ServiceStatus {
    Stopped,
    Starting,
    Running,
    Error,
}

enum class EventDecision {
    Pending,
    Added,
    Ignored,
}

enum class WhitelistFileFormat(
    val displayName: String,
    val description: String,
) {
    TrueHeader(
        displayName = "默认格式",
        description = "首行为 true，后续每行一个应用包名",
    ),
}

data class InstallEvent(
    val id: String,
    val packageName: String,
    val appLabel: String,
    val installedAt: Long,
    val sourceMode: MonitorMode,
    val decision: EventDecision = EventDecision.Pending,
)

data class WhitelistConfig(
    val importedUri: String? = null,
    val importedDisplayName: String? = null,
    val format: WhitelistFileFormat = WhitelistFileFormat.TrueHeader,
) {
    val usingDefaultFile: Boolean
        get() = importedUri.isNullOrBlank()
}

enum class RootAvailability {
    Unknown,
    Unsupported,
    Ready,
}

data class RootStatus(
    val availability: RootAvailability,
    val daemonRunning: Boolean,
    val message: String,
)

data class AppSettings(
    val monitorMode: MonitorMode = MonitorMode.Standard,
    val autoStartOnBoot: Boolean = false,
    val serviceStatus: ServiceStatus = ServiceStatus.Stopped,
)

data class UiMessage(
    val text: String,
    val isError: Boolean = false,
)

data class AppUiState(
    val settings: AppSettings = AppSettings(),
    val rootStatus: RootStatus = RootStatus(
        availability = RootAvailability.Unknown,
        daemonRunning = false,
        message = "Root 状态未检查",
    ),
    val whitelistConfig: WhitelistConfig = WhitelistConfig(),
    val whitelistEntries: List<String> = emptyList(),
    val installEvents: List<InstallEvent> = emptyList(),
    val latestMessage: UiMessage? = null,
)
