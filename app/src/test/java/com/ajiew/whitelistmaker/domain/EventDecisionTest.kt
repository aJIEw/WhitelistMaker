package com.ajiew.whitelistmaker.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class EventDecisionTest {
    @Test
    fun forNewInstall_marksWhitelistedPackageAsAdded() {
        assertEquals(EventDecision.Added, EventDecision.forNewInstall(isWhitelisted = true))
    }

    @Test
    fun forNewInstall_keepsUnlistedPackagePending() {
        assertEquals(EventDecision.Pending, EventDecision.forNewInstall(isWhitelisted = false))
    }
}
