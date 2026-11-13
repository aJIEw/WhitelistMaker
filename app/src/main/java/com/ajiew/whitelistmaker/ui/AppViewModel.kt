package com.ajiew.whitelistmaker.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.ajiew.whitelistmaker.WhitelistMakerApp
import com.ajiew.whitelistmaker.data.WhitelistRepository
import com.ajiew.whitelistmaker.domain.AppUiState
import com.ajiew.whitelistmaker.domain.InstallEvent

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: WhitelistRepository =
        (application as WhitelistMakerApp).appContainer.repository

    private val deletedEventGroupsFlow = MutableSharedFlow<List<InstallEvent>>()

    val uiState: StateFlow<AppUiState> = repository.uiState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppUiState(),
        )
    val deletedEventGroups = deletedEventGroupsFlow.asSharedFlow()

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

    fun removeFromWhitelist(packageName: String) {
        viewModelScope.launch {
            repository.removeFromWhitelist(packageName)
        }
    }

    fun ignoreEvent(eventId: String) {
        viewModelScope.launch {
            repository.ignoreEvent(eventId)
        }
    }

    fun revertIgnoredEvent(eventId: String) {
        viewModelScope.launch {
            repository.revertIgnoredEvent(eventId)
        }
    }

    fun deleteEventGroup(packageName: String) {
        viewModelScope.launch {
            repository.deleteEventGroup(packageName).takeIf(List<InstallEvent>::isNotEmpty)?.let { deletedEvents ->
                deletedEventGroupsFlow.emit(deletedEvents)
            }
        }
    }

    fun restoreDeletedEventGroup(events: List<InstallEvent>) {
        viewModelScope.launch {
            repository.restoreDeletedEventGroup(events)
        }
    }

    fun updateAutoStart(enabled: Boolean) {
        repository.updateAutoStart(enabled)
    }

    fun updateRevertEnabled(enabled: Boolean) {
        repository.updateRevertEnabled(enabled)
    }
}
