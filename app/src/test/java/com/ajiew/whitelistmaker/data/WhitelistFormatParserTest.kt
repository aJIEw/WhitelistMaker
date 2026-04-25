package com.ajiew.whitelistmaker.data

import com.ajiew.whitelistmaker.domain.WhitelistFileFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WhitelistFormatParserTest {
    @Test
    fun parseTrueHeader_acceptsCurrentFormat() {
        val result = WhitelistFormatParser.parse(
            content = "true\ncom.example.first\ncom.example.second\n",
            format = WhitelistFileFormat.TrueHeader,
        )

        assertTrue(result.isSuccess)
        assertEquals(
            listOf("com.example.first", "com.example.second"),
            result.getOrThrow().entries,
        )
    }

    @Test
    fun parseTrueHeader_rejectsMissingHeader() {
        val result = WhitelistFormatParser.parse(
            content = "com.example.first\n",
            format = WhitelistFileFormat.TrueHeader,
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun parseTrueHeader_rejectsInvalidPackageName() {
        val result = WhitelistFormatParser.parse(
            content = "true\nnot a package\n",
            format = WhitelistFileFormat.TrueHeader,
        )

        assertTrue(result.isFailure)
    }
}
