package com.ajiew.whitelistmaker.domain

enum class ServiceStatus {
    Stopped,
    Starting,
    Running,
    Error,
}

enum class EventDecision {
    Pending,
    Added,
    Ignored;

    companion object {
        fun forNewInstall(isWhitelisted: Boolean): EventDecision {
            return if (isWhitelisted) Added else Pending
        }
    }
}

enum class WhitelistFileFormat(
    val displayName: String,
    val description: String,
) {
    TrueHeader(
        displayName = "默认格式",
        description = "首行为 true 或 false，后续每行一个应用包名",
    ),
}

data class InstallEvent(
    val id: String,
    val packageName: String,
    val appLabel: String,
    val installedAt: Long,
    val decision: EventDecision = EventDecision.Pending,
)

data class WhitelistConfig(
    val importedUri: String? = null,
    val importedDisplayName: String? = null,
    val format: WhitelistFileFormat = WhitelistFileFormat.TrueHeader,
    val revertEnabled: Boolean = false,
) {
    val usingDefaultFile: Boolean
        get() = importedUri.isNullOrBlank()
}

data class WhitelistEntry(
    val packageName: String,
    val appLabel: String = "",
)

data class AppSettings(
    val autoStartOnBoot: Boolean = false,
    val serviceStatus: ServiceStatus = ServiceStatus.Stopped,
)

data class UiMessage(
    val text: String,
    val isError: Boolean = false,
)

data class AppUiState(
    val settings: AppSettings = AppSettings(),
    val whitelistConfig: WhitelistConfig = WhitelistConfig(),
    val whitelistEntries: List<WhitelistEntry> = emptyList(),
    val installEvents: List<InstallEvent> = emptyList(),
    val latestMessage: UiMessage? = null,
)
