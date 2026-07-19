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
}
