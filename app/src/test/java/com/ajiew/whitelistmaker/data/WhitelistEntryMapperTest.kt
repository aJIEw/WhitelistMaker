package com.ajiew.whitelistmaker.data

import org.junit.Assert.assertEquals
import org.junit.Test

class WhitelistEntryMapperTest {
    @Test
    fun map_preservesPackageOrderAndUsesEmptyLabelWhenResolutionFails() {
        val entries = WhitelistEntryMapper.map(
            packageNames = listOf(
                "com.example.installed",
                "com.example.missing",
            ),
            resolveLabel = { packageName ->
                if (packageName == "com.example.installed") "示例应用" else null
            },
        )

        assertEquals("com.example.installed", entries[0].packageName)
        assertEquals("示例应用", entries[0].appLabel)
        assertEquals("com.example.missing", entries[1].packageName)
        assertEquals("", entries[1].appLabel)
    }
}
