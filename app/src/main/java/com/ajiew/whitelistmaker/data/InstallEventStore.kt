package com.ajiew.whitelistmaker.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import com.ajiew.whitelistmaker.domain.EventDecision
import com.ajiew.whitelistmaker.domain.EventDecisionOrigin
import com.ajiew.whitelistmaker.domain.EventResolution
import com.ajiew.whitelistmaker.domain.InstallEventCollection
import com.ajiew.whitelistmaker.domain.InstallEvent
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class InstallEventStore(context: Context) {
    private val storageFile = File(context.filesDir, "install_events.json")
    private val eventsFlow = MutableStateFlow(readEventsFromDisk())
    private val writeMutex = Mutex()

    fun eventsFlow(): StateFlow<List<InstallEvent>> = eventsFlow.asStateFlow()

    suspend fun recordInstallEvent(
        packageName: String,
        appLabel: String,
        resolution: EventResolution,
    ): InstallEvent = withContext(Dispatchers.IO) {
        writeMutex.withLock {
            val updated = buildList {
                add(
                    InstallEvent(
                        id = UUID.randomUUID().toString(),
                        packageName = packageName,
                        appLabel = appLabel,
                        installedAt = System.currentTimeMillis(),
                        decision = resolution.decision,
                        decisionOrigin = resolution.origin,
                    )
                )
                addAll(eventsFlow.value.filterNot {
                    it.packageName == packageName && it.decision == EventDecision.Pending
                })
            }
            persist(updated)
            updated.first()
        }
    }

    suspend fun updateDecision(eventId: String, decision: EventDecision) = withContext(Dispatchers.IO) {
        writeMutex.withLock {
            val updated = eventsFlow.value.map { event ->
                if (event.id == eventId) {
                    event.copy(
                        decision = decision,
                        decisionOrigin = EventDecisionOrigin.Manual,
                    )
                } else {
                    event
                }
            }
            persist(updated)
        }
    }

    suspend fun deletePackageEvents(packageName: String): List<InstallEvent> = withContext(Dispatchers.IO) {
        writeMutex.withLock {
            val deletion = InstallEventCollection.deletePackage(eventsFlow.value, packageName)
            if (deletion.deleted.isNotEmpty()) {
                persist(deletion.remaining)
            }
            deletion.deleted
        }
    }

    suspend fun restoreEvents(events: List<InstallEvent>) = withContext(Dispatchers.IO) {
        writeMutex.withLock {
            if (events.isNotEmpty()) {
                persist(InstallEventCollection.restore(eventsFlow.value, events))
            }
        }
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
                    .put("decisionOrigin", event.decisionOrigin.name)
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
                        decisionOrigin = EventDecisionOrigin.valueOf(
                            item.optString("decisionOrigin", EventDecisionOrigin.Manual.name),
                        ),
                    )
                )
            }
        }.sortedByDescending { it.installedAt }
    }
}
