package com.ajiew.whitelistmaker.data

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ajiew.whitelistmaker.domain.EventDecision
import com.ajiew.whitelistmaker.domain.EventDecisionOrigin
import com.ajiew.whitelistmaker.domain.EventResolution
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class InstallEventStoreTest {
    private lateinit var context: Context
    private lateinit var storageFile: File

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        storageFile = File(context.filesDir, "install_events.json")
        storageFile.delete()
    }

    @After
    fun tearDown() {
        storageFile.delete()
    }

    @Test
    fun readLegacyEvent_defaultsMissingDecisionOriginToManual() {
        storageFile.writeText(
            """
            [{
              "id": "legacy",
              "packageName": "com.example.legacy",
              "appLabel": "Legacy",
              "installedAt": 100,
              "decision": "Added"
            }]
            """.trimIndent()
        )

        val event = InstallEventStore(context).eventsFlow().value.single()

        assertEquals(EventDecision.Added, event.decision)
        assertEquals(EventDecisionOrigin.Manual, event.decisionOrigin)
    }

    @Test
    fun recordInheritedEvent_roundTripsDecisionOrigin() = runBlocking {
        val store = InstallEventStore(context)
        val resolution = EventResolution(
            decision = EventDecision.Added,
            origin = EventDecisionOrigin.Inherited,
        )

        val recorded = store.recordInstallEvent(
            packageName = "com.example.inherited",
            appLabel = "Inherited",
            resolution = resolution,
        )
        val reloaded = InstallEventStore(context).eventsFlow().value.single()

        assertEquals(EventDecisionOrigin.Inherited, recorded.decisionOrigin)
        assertEquals(recorded, reloaded)
    }

    @Test
    fun revertIgnoredToPending_changesIgnoredEventToPending() = runBlocking {
        val store = InstallEventStore(context)
        val recorded = store.recordInstallEvent(
            packageName = "com.example.ignored",
            appLabel = "Ignored",
            resolution = EventResolution(
                decision = EventDecision.Ignored,
                origin = EventDecisionOrigin.Manual,
            ),
        )

        val reverted = store.revertIgnoredToPending(recorded.id)

        assertEquals(EventDecision.Pending, reverted?.decision)
        assertEquals(EventDecisionOrigin.Manual, reverted?.decisionOrigin)
        val reloaded = InstallEventStore(context).eventsFlow().value.single()
        assertEquals(EventDecision.Pending, reloaded.decision)
    }

    @Test
    fun revertIgnoredToPending_ignoresAddedEvent() = runBlocking {
        val store = InstallEventStore(context)
        val recorded = store.recordInstallEvent(
            packageName = "com.example.added",
            appLabel = "Added",
            resolution = EventResolution(
                decision = EventDecision.Added,
                origin = EventDecisionOrigin.Manual,
            ),
        )

        val reverted = store.revertIgnoredToPending(recorded.id)

        assertNull(reverted)
        val reloaded = InstallEventStore(context).eventsFlow().value.single()
        assertEquals(EventDecision.Added, reloaded.decision)
    }
}
