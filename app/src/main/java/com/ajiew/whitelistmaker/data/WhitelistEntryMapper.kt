package com.ajiew.whitelistmaker.data

import com.ajiew.whitelistmaker.domain.WhitelistEntry

internal object WhitelistEntryMapper {
    fun map(
        packageNames: List<String>,
        resolveLabel: (String) -> String?,
    ): List<WhitelistEntry> {
        return packageNames.map { packageName ->
            WhitelistEntry(
                packageName = packageName,
                appLabel = resolveLabel(packageName).orEmpty(),
            )
        }
    }
}
