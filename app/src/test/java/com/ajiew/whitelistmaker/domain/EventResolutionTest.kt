package com.ajiew.whitelistmaker.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class EventResolutionTest {
    @Test
    fun forNewInstall_marksWhitelistedPackageAsInheritedAdded() {
        val resolution = EventResolution.forNewInstall(isWhitelisted = true)

        assertEquals(EventDecision.Added, resolution.decision)
        assertEquals(EventDecisionOrigin.Inherited, resolution.origin)
    }

    @Test
    fun forNewInstall_keepsUnlistedPackagePendingAndManual() {
        val resolution = EventResolution.forNewInstall(isWhitelisted = false)

        assertEquals(EventDecision.Pending, resolution.decision)
        assertEquals(EventDecisionOrigin.Manual, resolution.origin)
    }
}
