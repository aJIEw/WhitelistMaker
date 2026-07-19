package com.ajiew.whitelistmaker.ui.home

import com.ajiew.whitelistmaker.domain.EventDecision
import com.ajiew.whitelistmaker.domain.EventDecisionOrigin
import com.ajiew.whitelistmaker.domain.InstallEvent

enum class InstallEventStatusTone {
    Pending,
    Added,
    Inherited,
    Ignored,
}

data class InstallEventStatus(
    val label: String,
    val tone: InstallEventStatusTone,
)

object InstallEventStatusPresentation {
    fun from(event: InstallEvent): InstallEventStatus {
        return when (event.decision) {
            EventDecision.Pending -> InstallEventStatus("待处理", InstallEventStatusTone.Pending)
            EventDecision.Added -> {
                if (event.decisionOrigin == EventDecisionOrigin.Inherited) {
                    InstallEventStatus("沿用 · 已添加", InstallEventStatusTone.Inherited)
                } else {
                    InstallEventStatus("已添加", InstallEventStatusTone.Added)
                }
            }
            EventDecision.Ignored -> InstallEventStatus("已忽略", InstallEventStatusTone.Ignored)
        }
    }
}
