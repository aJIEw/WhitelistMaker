package com.ajiew.whitelistmaker.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class InstallEventGroupsTest {
    @Test
    fun from_groupsSamePackageWithNewestEventFirst() {
        val older = installEvent(id = "older", installedAt = 100)
        val newest = installEvent(id = "newest", installedAt = 300)
        val middle = installEvent(id = "middle", installedAt = 200)

        val group = InstallEventGroups.from(listOf(older, newest, middle)).single()

        assertEquals(newest, group.latest)
        assertEquals(listOf(middle, older), group.history)
    }

    @Test
    fun visibleHistory_limitsCollapsedHistoryAndReturnsAllWhenExpanded() {
        val group = InstallEventGroups.from(
            listOf(
                installEvent(id = "4", installedAt = 400),
                installEvent(id = "3", installedAt = 300),
                installEvent(id = "2", installedAt = 200),
                installEvent(id = "1", installedAt = 100),
            )
        ).single()

        assertEquals(listOf("3", "2"), group.visibleHistory(expanded = false).map(InstallEvent::id))
        assertEquals(listOf("3", "2", "1"), group.visibleHistory(expanded = true).map(InstallEvent::id))
    }

    @Test
    fun from_keepsPackagesSeparateAndSortsGroupsByLatestInstall() {
        val olderPackage = installEvent(
            id = "older-package",
            packageName = "com.example.older",
            installedAt = 100,
        )
        val newerPackage = installEvent(
            id = "newer-package",
            packageName = "com.example.newer",
            installedAt = 300,
        )

        val groups = InstallEventGroups.from(listOf(olderPackage, newerPackage))

        assertEquals(listOf("com.example.newer", "com.example.older"), groups.map { it.packageName })
    }

    @Test
    fun visibleHistory_doesNotTruncateTwoOrFewerHistoryEvents() {
        val singleEventGroup = InstallEventGroups.from(
            listOf(installEvent(id = "single", installedAt = 100))
        ).single()
        val twoHistoryGroup = InstallEventGroups.from(
            listOf(
                installEvent(id = "3", installedAt = 300),
                installEvent(id = "2", installedAt = 200),
                installEvent(id = "1", installedAt = 100),
            )
        ).single()

        assertEquals(emptyList<InstallEvent>(), singleEventGroup.visibleHistory(expanded = false))
        assertEquals(listOf("2", "1"), twoHistoryGroup.visibleHistory(expanded = false).map(InstallEvent::id))
    }

    private fun installEvent(
        id: String,
        installedAt: Long,
        packageName: String = "com.example.repeated",
    ) = InstallEvent(
        id = id,
        packageName = packageName,
        appLabel = "Repeated",
        installedAt = installedAt,
    )
}
