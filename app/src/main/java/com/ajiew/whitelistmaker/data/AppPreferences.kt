package com.ajiew.whitelistmaker.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.ajiew.whitelistmaker.domain.AppSettings
import com.ajiew.whitelistmaker.domain.ServiceStatus
import com.ajiew.whitelistmaker.domain.WhitelistConfig
import com.ajiew.whitelistmaker.domain.WhitelistFileFormat

class AppPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val settingsFlow = MutableStateFlow(loadSettings())
    private val whitelistConfigFlow = MutableStateFlow(loadWhitelistConfig())

    fun settingsFlow(): StateFlow<AppSettings> = settingsFlow.asStateFlow()

    fun whitelistConfigFlow(): StateFlow<WhitelistConfig> = whitelistConfigFlow.asStateFlow()

    fun currentSettings(): AppSettings = settingsFlow.value

    fun currentWhitelistConfig(): WhitelistConfig = whitelistConfigFlow.value

    fun updateAutoStart(enabled: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_AUTO_START, enabled) }
        settingsFlow.value = settingsFlow.value.copy(autoStartOnBoot = enabled)
    }

    fun updateServiceStatus(status: ServiceStatus) {
        settingsFlow.value = settingsFlow.value.copy(serviceStatus = status)
    }

    fun resetRuntimeState() {
        sharedPreferences.edit { remove(KEY_SERVICE_STATUS) }
        settingsFlow.value = settingsFlow.value.copy(serviceStatus = ServiceStatus.Stopped)
    }

    fun updateWhitelistConfig(config: WhitelistConfig) {
        sharedPreferences.edit {
            putString(KEY_WHITELIST_URI, config.importedUri).putString(
                KEY_WHITELIST_NAME, config.importedDisplayName
            ).putString(KEY_WHITELIST_FORMAT, config.format.name)
        }
        whitelistConfigFlow.value = config
    }

    fun updateWhitelistFormat(format: WhitelistFileFormat) {
        sharedPreferences.edit { putString(KEY_WHITELIST_FORMAT, format.name) }
        whitelistConfigFlow.value = whitelistConfigFlow.value.copy(format = format)
    }

    private fun loadSettings(): AppSettings {
        return AppSettings(
            autoStartOnBoot = sharedPreferences.getBoolean(KEY_AUTO_START, false),
            serviceStatus = ServiceStatus.Stopped,
        )
    }

    private fun loadWhitelistConfig(): WhitelistConfig {
        return WhitelistConfig(
            importedUri = sharedPreferences.getString(KEY_WHITELIST_URI, null),
            importedDisplayName = sharedPreferences.getString(KEY_WHITELIST_NAME, null),
            format = enumValueOfOrDefault(
                sharedPreferences.getString(KEY_WHITELIST_FORMAT, null),
                WhitelistFileFormat.TrueHeader,
            ),
        )
    }

    private fun <T : Enum<T>> enumValueOfOrDefault(raw: String?, default: T): T {
        return raw?.runCatching { java.lang.Enum.valueOf(default.declaringJavaClass, this) }
            ?.getOrNull() ?: default
    }

    private companion object {
        private const val PREFS_NAME = "whitelist_prefs"
        private const val KEY_AUTO_START = "auto_start"
        private const val KEY_SERVICE_STATUS = "service_status"
        private const val KEY_WHITELIST_URI = "whitelist_uri"
        private const val KEY_WHITELIST_NAME = "whitelist_name"
        private const val KEY_WHITELIST_FORMAT = "whitelist_format"
    }
}
