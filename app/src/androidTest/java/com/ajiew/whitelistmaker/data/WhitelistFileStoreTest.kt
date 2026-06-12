package com.ajiew.whitelistmaker.data

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class WhitelistFileStoreTest {
    private lateinit var context: Context
    private lateinit var whitelistFile: File
    private lateinit var store: WhitelistFileStore

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        whitelistFile = File(context.filesDir, DEFAULT_FILE_NAME)
        whitelistFile.delete()
        store = WhitelistFileStore(
            context = context,
            preferences = AppPreferences(context),
        )
    }

    @After
    fun tearDown() {
        whitelistFile.delete()
    }

    @Test
    fun removePackage_removesExistingPackageAndPreservesFileFormat() = runBlocking {
        store.replaceFromText(
            """
            true
            com.example.first
            com.example.second
            """.trimIndent(),
        ).getOrThrow()

        val removed = store.removePackage("com.example.first")

        assertTrue(removed)
        assertEquals(listOf("com.example.second"), store.entriesFlow().value)
        assertEquals("true\ncom.example.second\n", whitelistFile.readText())
    }

    @Test
    fun removePackage_returnsFalseWithoutRewritingWhenPackageDoesNotExist() = runBlocking {
        store.replaceFromText(
            """
            true
            com.example.first
            """.trimIndent(),
        ).getOrThrow()
        val contentBeforeRemoval = whitelistFile.readText()
        assertTrue(whitelistFile.setLastModified(UNCHANGED_FILE_TIMESTAMP))
        val lastModifiedBeforeRemoval = whitelistFile.lastModified()

        val removed = store.removePackage("com.example.missing")

        assertFalse(removed)
        assertEquals(contentBeforeRemoval, whitelistFile.readText())
        assertEquals(lastModifiedBeforeRemoval, whitelistFile.lastModified())
        assertEquals(listOf("com.example.first"), store.entriesFlow().value)
    }

    @Test
    fun removePackage_removesLastPackageAndLeavesValidEmptyWhitelist() = runBlocking {
        store.replaceFromText(
            """
            true
            com.example.only
            """.trimIndent(),
        ).getOrThrow()

        val removed = store.removePackage("com.example.only")

        assertTrue(removed)
        assertTrue(store.entriesFlow().value.isEmpty())
        assertEquals("true\n", whitelistFile.readText())
        assertTrue(
            WhitelistFormatParser.parse(
                content = whitelistFile.readText(),
                format = AppPreferences(context).currentWhitelistConfig().format,
            ).isSuccess,
        )
    }

    private companion object {
        const val PREFERENCES_NAME = "whitelist_prefs"
        const val DEFAULT_FILE_NAME = "default_whitelist.txt"
        const val UNCHANGED_FILE_TIMESTAMP = 1_000L
    }
}
