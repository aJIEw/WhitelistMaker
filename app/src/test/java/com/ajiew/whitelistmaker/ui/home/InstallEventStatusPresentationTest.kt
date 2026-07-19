package com.ajiew.whitelistmaker.ui.home

import com.ajiew.whitelistmaker.domain.EventDecision
import com.ajiew.whitelistmaker.domain.EventDecisionOrigin
import com.ajiew.whitelistmaker.domain.InstallEvent
import org.junit.Assert.assertEquals
import org.junit.Test

class InstallEventStatusPresentationTest {
    @Test
    fun from_labelsManualAddedEventAsAdded() {
        val event = InstallEvent(
            id = "manual",
            packageName = "com.example.app",
            appLabel = "Example",
            installedAt = 100,
            decision = EventDecision.Added,
        )

        val status = InstallEventStatusPresentation.from(event)

        assertEquals("已添加", status.label)
        assertEquals(InstallEventStatusTone.Added, status.tone)
    }

    @Test
    fun from_labelsInheritedAddedEventAsReusedResult() {
        val event = InstallEvent(
            id = "inherited",
            packageName = "com.example.app",
            appLabel = "Example",
            installedAt = 100,
            decision = EventDecision.Added,
            decisionOrigin = EventDecisionOrigin.Inherited,
        )

        val status = InstallEventStatusPresentation.from(event)

        assertEquals("沿用 · 已添加", status.label)
        assertEquals(InstallEventStatusTone.Inherited, status.tone)
    }
}
