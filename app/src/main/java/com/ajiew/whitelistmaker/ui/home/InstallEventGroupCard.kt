package com.ajiew.whitelistmaker.ui.home

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.NotInterested
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ajiew.whitelistmaker.domain.EventDecision
import com.ajiew.whitelistmaker.domain.InstallEvent
import com.ajiew.whitelistmaker.domain.InstallEventGroup
import com.ajiew.whitelistmaker.ui.AppActions
import com.ajiew.whitelistmaker.ui.PreviewDefaults
import com.ajiew.whitelistmaker.ui.components.StatusPill
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.Instant

@Preview
@Composable
private fun IgnoredInstallEventGroupCardPreview() {
    InstallEventGroupCard(
        group = InstallEventGroup(
            latest = InstallEvent(
                id = "ignored",
                packageName = "com.example.notes",
                appLabel = "Notes",
                installedAt = 1_713_955_200_000,
                decision = EventDecision.Ignored,
            ),
            history = emptyList(),
        ),
        actions = PreviewDefaults.actions,
    )
}

@Composable
internal fun InstallEventGroupCard(
    group: InstallEventGroup,
    actions: AppActions,
    modifier: Modifier = Modifier,
) {
    var historyExpanded by rememberSaveable(group.packageName) { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { distance -> distance * 0.35f },
    )
    LaunchedEffect(dismissState.currentValue, group.packageName) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            actions.onDeleteEventGroup(group.packageName)
        }
    }
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            DeleteActionBackground(
                revealed = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart,
            )
        },
        content = {
            Card(shape = RoundedCornerShape(24.dp)) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = group.latest.appLabel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = group.packageName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    EventMetadataRow(
                        event = group.latest,
                        isInheritedAdded = group.isInheritedAdded(group.latest),
                    )
                    if (group.latest.decision == EventDecision.Pending) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    actions.onAddToWhitelist(group.latest.id, group.packageName)
                                },
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Outlined.TaskAlt, contentDescription = null)
                                Spacer(Modifier.padding(horizontal = 4.dp))
                                Text("添加")
                            }
                            OutlinedButton(
                                onClick = { actions.onIgnoreEvent(group.latest.id) },
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Outlined.NotInterested, contentDescription = null)
                                Spacer(Modifier.padding(horizontal = 4.dp))
                                Text("忽略")
                            }
                        }
                    }
                    if (group.latest.decision == EventDecision.Ignored) {
                        OutlinedButton(
                            onClick = { actions.onRevertIgnoredEvent(group.latest.id) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.Undo, contentDescription = null)
                            Spacer(Modifier.padding(horizontal = 4.dp))
                            Text("撤回忽略")
                        }
                    }
                    if (group.history.isNotEmpty()) {
                        HorizontalDivider()
                        Text(
                            text = "历史记录（${group.history.size}）",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        group.visibleHistory(historyExpanded).forEach { event ->
                            EventMetadataRow(
                                event = event,
                                isInheritedAdded = group.isInheritedAdded(event),
                            )
                        }
                        if (group.history.size > InstallEventGroup.DEFAULT_VISIBLE_HISTORY_COUNT) {
                            TextButton(
                                onClick = { historyExpanded = !historyExpanded },
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                            ) {
                                Text(
                                    if (historyExpanded) {
                                        "收起"
                                    } else {
                                        "查看全部（${group.history.size}）"
                                    },
                                )
                            }
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun EventMetadataRow(
    event: InstallEvent,
    isInheritedAdded: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EventStatusPill(event, isInheritedAdded)
        Text(
            text = formatTimestamp(event.installedAt),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EventStatusPill(
    event: InstallEvent,
    isInheritedAdded: Boolean,
) {
    val status = InstallEventStatusPresentation.from(event, isInheritedAdded)
    val darkTheme = isSystemInDarkTheme()
    StatusPill(
        label = status.label,
        color = when (status.tone) {
            InstallEventStatusTone.Pending -> MaterialTheme.colorScheme.tertiaryContainer
            InstallEventStatusTone.Added -> if (darkTheme) {
                Color(0xFF1C5437)
            } else {
                Color(0xFFD6F5E3)
            }
            InstallEventStatusTone.Inherited -> if (darkTheme) {
                Color(0xFF354656)
            } else {
                Color(0xFFDCE6F0)
            }
            InstallEventStatusTone.Ignored -> MaterialTheme.colorScheme.surfaceVariant
        },
        contentColor = when (status.tone) {
            InstallEventStatusTone.Pending -> MaterialTheme.colorScheme.onTertiaryContainer
            InstallEventStatusTone.Added -> if (darkTheme) {
                Color(0xFFC0F0D2)
            } else {
                Color(0xFF0B3D24)
            }
            InstallEventStatusTone.Inherited -> if (darkTheme) {
                Color(0xFFD5E4F2)
            } else {
                Color(0xFF273847)
            }
            InstallEventStatusTone.Ignored -> MaterialTheme.colorScheme.onSurfaceVariant
        },
    )
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())
    return formatter.format(Instant.ofEpochMilli(timestamp))
}
