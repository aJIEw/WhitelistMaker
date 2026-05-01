package com.ajiew.whitelistmaker.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.ajiew.whitelistmaker.WhitelistMakerApp
import com.ajiew.whitelistmaker.data.WhitelistRepository
import com.ajiew.whitelistmaker.domain.AppUiState
import com.ajiew.whitelistmaker.domain.InstallEvent
import com.ajiew.whitelistmaker.domain.MonitorMode
import com.ajiew.whitelistmaker.domain.RootAvailability
import com.ajiew.whitelistmaker.domain.RootStatus
import com.ajiew.whitelistmaker.domain.WhitelistFileFormat

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: WhitelistRepository =
        (application as WhitelistMakerApp).appContainer.repository

    private val rootStatusFlow = MutableStateFlow(
        RootStatus(
            availability = RootAvailability.Unknown,
            daemonRunning = false,
            message = "Root 状态检查中",
        )
    )
    private val deletedEventsFlow = MutableSharedFlow<InstallEvent>()

    val uiState: StateFlow<AppUiState> = repository.uiState(rootStatusFlow)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppUiState(),
        )
    val deletedEvents = deletedEventsFlow.asSharedFlow()

    init {
        refreshRootStatus()
    }

    fun refreshRootStatus() {
        viewModelScope.launch {
            rootStatusFlow.value = repository.refreshRootStatus()
        }
    }

    fun importWhitelist(uri: Uri) {
        viewModelScope.launch {
            repository.importWhitelist(uri)
        }
    }

    fun exportWhitelist(uri: Uri) {
        viewModelScope.launch {
            repository.exportWhitelist(uri)
        }
    }

    fun copyWhitelistToClipboard() {
        viewModelScope.launch {
            repository.copyWhitelistToClipboard()
        }
    }

    fun pasteWhitelistFromClipboard() {
        viewModelScope.launch {
            repository.pasteWhitelistFromClipboard()
        }
    }

    fun addEventToWhitelist(eventId: String, packageName: String) {
        viewModelScope.launch {
            repository.addEventToWhitelist(eventId, packageName)
        }
    }

    fun ignoreEvent(eventId: String) {
        viewModelScope.launch {
            repository.ignoreEvent(eventId)
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            repository.deleteEvent(eventId)?.let { deletedEvent ->
                deletedEventsFlow.emit(deletedEvent)
            }
        }
    }

    fun restoreDeletedEvent(event: InstallEvent) {
        viewModelScope.launch {
            repository.restoreDeletedEvent(event)
        }
    }

    fun updateMonitorMode(mode: MonitorMode) {
        repository.updateMonitorMode(mode)
        if (mode == MonitorMode.EnhancedRoot) {
            refreshRootStatus()
        }
    }

    fun updateAutoStart(enabled: Boolean) {
        repository.updateAutoStart(enabled)
    }

    fun updateWhitelistFormat(format: WhitelistFileFormat) {
        repository.updateWhitelistFormat(format)
    }

    fun startRootDaemon() {
        viewModelScope.launch {
            rootStatusFlow.value = repository.startRootDaemon()
        }
    }

    fun stopRootDaemon() {
        viewModelScope.launch {
            rootStatusFlow.value = repository.stopRootDaemon()
        }
    }
}
