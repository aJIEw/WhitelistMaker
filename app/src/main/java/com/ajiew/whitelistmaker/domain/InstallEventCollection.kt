package com.ajiew.whitelistmaker.domain

data class InstallEventDeletion(
    val deleted: List<InstallEvent>,
    val remaining: List<InstallEvent>,
)

object InstallEventCollection {
    fun deletePackage(
        events: List<InstallEvent>,
        packageName: String,
    ): InstallEventDeletion {
        val deleted = events
            .filter { it.packageName == packageName }
            .sortedByDescending(InstallEvent::installedAt)
        val remaining = events
            .filterNot { it.packageName == packageName }
            .sortedByDescending(InstallEvent::installedAt)
        return InstallEventDeletion(deleted = deleted, remaining = remaining)
    }

    fun restore(
        current: List<InstallEvent>,
        deleted: List<InstallEvent>,
    ): List<InstallEvent> {
        val deletedIds = deleted.mapTo(mutableSetOf(), InstallEvent::id)
        val seenPackages = mutableSetOf<String>()
        return (current.filterNot { it.id in deletedIds } + deleted)
            .sortedByDescending(InstallEvent::installedAt)
            .filter { event ->
                val hasNewerEvent = event.packageName in seenPackages
                seenPackages.add(event.packageName)
                event.decision != EventDecision.Pending || !hasNewerEvent
            }
    }
}
