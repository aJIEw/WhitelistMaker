package com.ajiew.whitelistmaker.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class InstallEventCollectionTest {
    @Test
    fun deletePackage_removesAndReturnsEveryMatchingEvent() {
        val first = installEvent("first", "com.example.repeated", 100).copy(
            decision = EventDecision.Added,
        )
        val other = installEvent("other", "com.example.other", 200)
        val latest = installEvent("latest", "com.example.repeated", 300)

        val deletion = InstallEventCollection.deletePackage(
            events = listOf(first, other, latest),
            packageName = "com.example.repeated",
        )

        assertEquals(listOf(latest, first), deletion.deleted)
        assertEquals(listOf(other), deletion.remaining)
    }

    @Test
    fun restore_reinsertsDeletedEventsWithoutDuplicateIds() {
        val first = installEvent("first", "com.example.repeated", 100).copy(
            decision = EventDecision.Added,
        )
        val other = installEvent("other", "com.example.other", 200)
        val latest = installEvent("latest", "com.example.repeated", 300)

        val restored = InstallEventCollection.restore(
            current = listOf(other, latest),
            deleted = listOf(latest, first),
        )

        assertEquals(listOf(latest, other, first), restored)
    }

    @Test
    fun restore_keepsOnlyNewestPendingEventForPackage() {
        val oldPending = installEvent(
            id = "old-pending",
            packageName = "com.example.repeated",
            installedAt = 100,
        )
        val processedHistory = oldPending.copy(
            id = "processed-history",
            installedAt = 50,
            decision = EventDecision.Added,
        )
        val newPending = oldPending.copy(id = "new-pending", installedAt = 300)

        val restored = InstallEventCollection.restore(
            current = listOf(newPending),
            deleted = listOf(oldPending, processedHistory),
        )

        assertEquals(listOf(newPending, processedHistory), restored)
    }

    @Test
    fun restore_dropsOldPendingWhenPackageHasNewerAddedEvent() {
        val oldPending = installEvent(
            id = "old-pending",
            packageName = "com.example.repeated",
            installedAt = 100,
        )
        val newAdded = oldPending.copy(
            id = "new-added",
            installedAt = 300,
            decision = EventDecision.Added,
        )

        val restored = InstallEventCollection.restore(
            current = listOf(newAdded),
            deleted = listOf(oldPending),
        )

        assertEquals(listOf(newAdded), restored)
    }

    private fun installEvent(id: String, packageName: String, installedAt: Long) = InstallEvent(
        id = id,
        packageName = packageName,
        appLabel = id,
        installedAt = installedAt,
    )
}
