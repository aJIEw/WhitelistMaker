package com.ajiew.whitelistmaker.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material.icons.outlined.NotInterested
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ajiew.whitelistmaker.domain.AppUiState
import com.ajiew.whitelistmaker.domain.EventDecision
import com.ajiew.whitelistmaker.domain.InstallEvent
import com.ajiew.whitelistmaker.domain.InstallEventGroup
import com.ajiew.whitelistmaker.domain.InstallEventGroups
import com.ajiew.whitelistmaker.domain.ServiceStatus
import com.ajiew.whitelistmaker.ui.AppActions
import com.ajiew.whitelistmaker.ui.PreviewDefaults
import com.ajiew.whitelistmaker.ui.components.EmptyStateCard
import com.ajiew.whitelistmaker.ui.components.MessageCard
import com.ajiew.whitelistmaker.ui.components.SectionHeader
import com.ajiew.whitelistmaker.ui.components.StatusPill
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
internal fun HomeRoute(
    uiState: AppUiState,
    actions: AppActions,
    innerPadding: PaddingValues,
) {
    val eventGroups = remember(uiState.installEvents) {
        InstallEventGroups.from(uiState.installEvents)
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = innerPadding.calculateTopPadding() + 12.dp,
            end = 20.dp,
            bottom = innerPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            HeroStatusCard(uiState, actions)
        }
        item {
            WhitelistFileCard(uiState, actions)
        }
        uiState.latestMessage?.let { message ->
            item {
                MessageCard(message.text, message.isError)
            }
        }
        item {
            SectionHeader("新安装的应用", "${uiState.installEvents.size} 条记录")
        }
        if (uiState.installEvents.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "还没有监听到新应用",
                    body = "开启服务后，新安装的应用会出现在这里，你可以逐个决定是否追加到白名单。",
                )
            }
        } else {
            items(eventGroups, key = { it.packageName }) { group ->
                InstallEventGroupCard(
                    group = group,
                    actions = actions,
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@Preview
@Composable
private fun HomeRoutePreview() {
    HomeRoute(
        uiState = PreviewDefaults.uiState,
        actions = PreviewDefaults.actions,
        innerPadding = PaddingValues(),
    )
}

@Preview
@Composable
private fun HeroStatusCardPreview() {
    HeroStatusCard(
        uiState = PreviewDefaults.uiState,
        actions = PreviewDefaults.actions,
    )
}

@Composable
private fun HeroStatusCard(
    uiState: AppUiState,
    actions: AppActions,
) {
    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF0B5CAD), Color(0xFF38A3A5)),
                    ),
                )
                .padding(22.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "安装白名单助手",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "监听新安装的应用，并由你决定哪些包名要追加到白名单文件。",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFEAF6FF),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatusPill(
                        label = when (uiState.settings.serviceStatus) {
                            ServiceStatus.Stopped -> "服务已停止"
                            ServiceStatus.Starting -> "服务启动中"
                            ServiceStatus.Running -> "服务运行中"
                            ServiceStatus.Error -> "服务异常"
                        },
                        color = Color.White.copy(alpha = 0.18f),
                        contentColor = Color.White,
                    )
                }
                Button(
                    onClick = {
                        actions.onToggleService(uiState.settings.serviceStatus != ServiceStatus.Running)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.settings.serviceStatus == ServiceStatus.Running) {
                            Color(0xFFD20808)
                        } else {
                            Color(0xFF1BCE71)
                        },
                        contentColor = Color.White,
                    ),
                ) {
                    Text(
                        if (uiState.settings.serviceStatus == ServiceStatus.Running) {
                            "停止监听服务"
                        } else {
                            "开启监听服务"
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun WhitelistFileCard(
    uiState: AppUiState,
    actions: AppActions,
) {
    Card(shape = RoundedCornerShape(28.dp)) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionHeader(
                title = "白名单文件",
                subtitle = if (uiState.whitelistConfig.usingDefaultFile) "默认文件" else "用户导入文件",
            )
            Text(
                text = if (uiState.whitelistConfig.usingDefaultFile) {
                    "当前未指定文件，应用会在私有目录使用${uiState.whitelistConfig.format.displayName}。"
                } else {
                    "当前写入目标：${uiState.whitelistConfig.importedDisplayName ?: "已导入文件"}"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = actions.onImportWhitelist, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.FileOpen, contentDescription = null)
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    Text("导入文件")
                }
                OutlinedButton(
                    onClick = actions.onPasteWhitelistFromClipboard,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        Icons.Outlined.ContentPaste,
                        modifier = Modifier.size(18.dp),
                        contentDescription = null,
                    )
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    Text("从剪贴板复制", maxLines = 1, softWrap = false)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InstallEventGroupCard(
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
                    EventMetadataRow(group.latest)
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
                    if (group.history.isNotEmpty()) {
                        HorizontalDivider()
                        Text(
                            text = "历史记录（${group.history.size}）",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        group.visibleHistory(historyExpanded).forEach { event ->
                            EventMetadataRow(event)
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
private fun EventMetadataRow(event: InstallEvent) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EventStatusPill(event.decision)
        Text(
            text = formatTimestamp(event.installedAt),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EventStatusPill(decision: EventDecision) {
    StatusPill(
        label = when (decision) {
            EventDecision.Pending -> "待处理"
            EventDecision.Added -> "已添加"
            EventDecision.Ignored -> "已忽略"
        },
        color = when (decision) {
            EventDecision.Pending -> MaterialTheme.colorScheme.tertiaryContainer
            EventDecision.Added -> MaterialTheme.colorScheme.primaryContainer
            EventDecision.Ignored -> MaterialTheme.colorScheme.surfaceVariant
        },
        contentColor = when (decision) {
            EventDecision.Pending -> MaterialTheme.colorScheme.onTertiaryContainer
            EventDecision.Added -> MaterialTheme.colorScheme.onPrimaryContainer
            EventDecision.Ignored -> MaterialTheme.colorScheme.onSurfaceVariant
        },
    )
}

@Preview
@Composable
private fun DeleteActionBackgroundPreview() {
    Box(
        modifier = Modifier
            .size(400.dp, 120.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(color = Color(96, 20, 16))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        DeleteActionBackgroundTextIcon()
    }
}

@Composable
private fun DeleteActionBackground(
    revealed: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        if (revealed) {
            DeleteActionBackgroundTextIcon()
        }
    }
}

@Composable
private fun DeleteActionBackgroundTextIcon() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "左\n滑\n删\n除",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFEAB8B8),
        )
        Icon(
            imageVector = Icons.Outlined.Delete,
            modifier = Modifier.size(32.dp),
            contentDescription = "删除",
            tint = Color(0xFFEAB8B8),
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())
    return formatter.format(Instant.ofEpochMilli(timestamp))
}
