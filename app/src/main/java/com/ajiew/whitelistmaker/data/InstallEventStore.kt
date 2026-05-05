package com.ajiew.whitelistmaker.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import com.ajiew.whitelistmaker.domain.EventDecision
import com.ajiew.whitelistmaker.domain.InstallEvent
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class InstallEventStore(context: Context) {
    private val storageFile = File(context.filesDir, "install_events.json")
    private val eventsFlow = MutableStateFlow(readEventsFromDisk())

    fun eventsFlow(): StateFlow<List<InstallEvent>> = eventsFlow.asStateFlow()

    suspend fun recordInstallEvent(
        packageName: String,
        appLabel: String,
    ): InstallEvent = withContext(Dispatchers.IO) {
        val updated = buildList {
            add(
                InstallEvent(
                    id = UUID.randomUUID().toString(),
                    packageName = packageName,
                    appLabel = appLabel,
                    installedAt = System.currentTimeMillis(),
                )
            )
            addAll(eventsFlow.value.filterNot { it.packageName == packageName && it.decision == EventDecision.Pending })
        }
        persist(updated)
        updated.first()
    }

    suspend fun updateDecision(eventId: String, decision: EventDecision) = withContext(Dispatchers.IO) {
        val updated = eventsFlow.value.map { event ->
            if (event.id == eventId) {
                event.copy(decision = decision)
            } else {
                event
            }
        }
        persist(updated)
    }

    suspend fun deleteEvent(eventId: String): InstallEvent? = withContext(Dispatchers.IO) {
        val deletedEvent = eventsFlow.value.firstOrNull { it.id == eventId }
        val updated = eventsFlow.value.filterNot { it.id == eventId }
        persist(updated)
        deletedEvent
    }

    suspend fun restoreEvent(event: InstallEvent) = withContext(Dispatchers.IO) {
        val updated = buildList {
            add(event)
            addAll(eventsFlow.value.filterNot { it.id == event.id })
        }
        persist(updated)
    }

    private fun persist(events: List<InstallEvent>) {
        val payload = JSONArray()
        events.forEach { event ->
            payload.put(
                JSONObject()
                    .put("id", event.id)
                    .put("packageName", event.packageName)
                    .put("appLabel", event.appLabel)
                    .put("installedAt", event.installedAt)
                    .put("decision", event.decision.name)
            )
        }
        storageFile.writeText(payload.toString())
        eventsFlow.value = events.sortedByDescending { it.installedAt }
    }

    private fun readEventsFromDisk(): List<InstallEvent> {
        if (!storageFile.exists()) {
            return emptyList()
        }
        val raw = storageFile.readText().ifBlank { "[]" }
        val jsonArray = JSONArray(raw)
        return buildList {
            for (index in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(index)
                add(
                    InstallEvent(
                        id = item.getString("id"),
                        packageName = item.getString("packageName"),
                        appLabel = item.optString("appLabel", item.getString("packageName")),
                        installedAt = item.getLong("installedAt"),
                        decision = EventDecision.valueOf(item.optString("decision", EventDecision.Pending.name)),
                    )
                )
            }
        }.sortedByDescending { it.installedAt }
    }
}
