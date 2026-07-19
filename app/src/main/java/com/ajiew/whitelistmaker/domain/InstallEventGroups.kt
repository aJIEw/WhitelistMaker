package com.ajiew.whitelistmaker.domain

data class InstallEventGroup(
    val latest: InstallEvent,
    val history: List<InstallEvent>,
) {
    val packageName: String
        get() = latest.packageName

    fun visibleHistory(expanded: Boolean): List<InstallEvent> {
        return if (expanded) history else history.take(DEFAULT_VISIBLE_HISTORY_COUNT)
    }

    companion object {
        const val DEFAULT_VISIBLE_HISTORY_COUNT = 2
    }
}

object InstallEventGroups {
    fun from(events: List<InstallEvent>): List<InstallEventGroup> {
        return events
            .groupBy(InstallEvent::packageName)
            .values
            .map { packageEvents ->
                val sortedEvents = packageEvents.sortedByDescending(InstallEvent::installedAt)
                InstallEventGroup(
                    latest = sortedEvents.first(),
                    history = sortedEvents.drop(1),
                )
            }
            .sortedByDescending { it.latest.installedAt }
    }
}
