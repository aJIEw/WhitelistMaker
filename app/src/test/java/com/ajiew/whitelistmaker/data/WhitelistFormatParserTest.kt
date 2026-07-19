package com.ajiew.whitelistmaker.data

import com.ajiew.whitelistmaker.domain.WhitelistFileFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WhitelistFormatParserTest {
    @Test
    fun parseBooleanHeader_acceptsTrueAndRemovesDuplicates() {
        val result = WhitelistFormatParser.parse(
            content = "true\ncom.example.first\ncom.example.second\ncom.example.first\n",
            format = WhitelistFileFormat.TrueHeader,
        )

        assertTrue(result.isSuccess)
        assertEquals(
            listOf("com.example.first", "com.example.second"),
            result.getOrThrow().entries,
        )
    }

    @Test
    fun parseBooleanHeader_acceptsFalse() {
        val result = WhitelistFormatParser.parse(
            content = "false\ncom.example.first\n",
            format = WhitelistFileFormat.TrueHeader,
        )

        assertTrue(result.isSuccess)
        assertEquals(listOf("com.example.first"), result.getOrThrow().entries)
    }

    @Test
    fun parseBooleanHeader_rejectsMissingHeader() {
        val result = WhitelistFormatParser.parse(
            content = "com.example.first\n",
            format = WhitelistFileFormat.TrueHeader,
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun parseBooleanHeader_rejectsInvalidHeader() {
        val result = WhitelistFormatParser.parse(
            content = "enabled\ncom.example.first\n",
            format = WhitelistFileFormat.TrueHeader,
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun parseBooleanHeader_rejectsInvalidPackageName() {
        val result = WhitelistFormatParser.parse(
            content = "true\nnot a package\n",
            format = WhitelistFileFormat.TrueHeader,
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun buildBooleanHeader_defaultsToFalseWhenRevertDisabled() {
        val content = WhitelistFormatParser.build(
            entries = listOf("com.example.first"),
            format = WhitelistFileFormat.TrueHeader,
            revertEnabled = false,
        )

        assertEquals("false\ncom.example.first\n", content)
    }

    @Test
    fun buildBooleanHeader_usesTrueWhenRevertEnabled() {
        val content = WhitelistFormatParser.build(
            entries = listOf("com.example.first"),
            format = WhitelistFileFormat.TrueHeader,
            revertEnabled = true,
        )

        assertEquals("true\ncom.example.first\n", content)
    }
}
