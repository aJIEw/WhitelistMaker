package me.ajiew.bypasslistmaster.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.ToggleOff
import androidx.compose.material.icons.outlined.ToggleOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import me.ajiew.bypasslistmaster.domain.AppUiState
import me.ajiew.bypasslistmaster.domain.EventDecision
import me.ajiew.bypasslistmaster.domain.InstallEvent
import me.ajiew.bypasslistmaster.domain.MonitorMode
import me.ajiew.bypasslistmaster.domain.RootAvailability
import me.ajiew.bypasslistmaster.domain.ServiceStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class AppRoute {
    Home, Whitelist, Settings,
}

@Composable
fun AppScreen(
    uiState: AppUiState,
    actions: AppActions,
    initialRoute: AppRoute,
    deletedEvents: Flow<InstallEvent>,
) {
    var selectedRoute by rememberSaveable { mutableStateOf(initialRoute) }
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(deletedEvents) {
        deletedEvents.collectLatest { deletedEvent ->
            val result = snackbarHostState.showSnackbar(
                message = "${deletedEvent.appLabel} 已删除",
                actionLabel = "撤销",
                withDismissAction = true,
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) {
                actions.onRestoreDeletedEvent(deletedEvent)
            }
        }
    }
    Scaffold(
        topBar = {
            AppTopBar(selectedRoute)
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        bottomBar = {
            NavigationBar(modifier = Modifier.navigationBarsPadding()) {
                listOf(
                    AppRoute.Home to "主页",
                    AppRoute.Whitelist to "白名单",
                    AppRoute.Settings to "设置",
                ).forEach { (route, label) ->
                    NavigationBarItem(
                        selected = selectedRoute == route,
                        onClick = { selectedRoute = route },
                        icon = {
                            Icon(
                                imageVector = when (route) {
                                    AppRoute.Home -> Icons.Outlined.NotificationsActive
                                    AppRoute.Whitelist -> Icons.Outlined.Shield
                                    AppRoute.Settings -> Icons.Outlined.Settings
                                },
                                contentDescription = label,
                            )
                        },
                        label = { Text(label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        Surface(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            when (selectedRoute) {
                AppRoute.Home -> HomeRoute(uiState, actions, innerPadding)
                AppRoute.Whitelist -> WhitelistRoute(uiState, actions, innerPadding)
                AppRoute.Settings -> SettingsRoute(uiState, actions, innerPadding)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar(route: AppRoute) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = when (route) {
                    AppRoute.Home -> "安装监听"
                    AppRoute.Whitelist -> "白名单"
                    AppRoute.Settings -> "设置"
                },
                fontWeight = FontWeight.SemiBold,
            )
        },
    )
}

@Composable
private fun HomeRoute(
    uiState: AppUiState,
    actions: AppActions,
    innerPadding: PaddingValues,
) {
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
            items(uiState.installEvents, key = { it.id }) { event ->
                InstallEventCard(event, actions)
            }
        }
    }
}

@Composable
private fun WhitelistRoute(
    uiState: AppUiState,
    actions: AppActions,
    innerPadding: PaddingValues,
) {
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
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "当前共有 ${uiState.whitelistEntries.size} 个包名",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = if (uiState.whitelistConfig.usingDefaultFile) {
                            "正在使用应用默认白名单文件，首行为 true，后续每行一个包名。"
                        } else {
                            "当前写入文件：${uiState.whitelistConfig.importedDisplayName ?: "已导入文件"}"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = actions.onExportWhitelist,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.FileDownload, contentDescription = null)
                            Spacer(Modifier.padding(horizontal = 4.dp))
                            Text("导出到文件")
                        }
                        OutlinedButton(
                            onClick = actions.onCopyWhitelist,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.ContentCopy, contentDescription = null)
                            Spacer(Modifier.padding(horizontal = 4.dp))
                            Text("复制到剪贴板")
                        }
                    }
                }
            }
        }
        if (uiState.whitelistEntries.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "白名单还是空的",
                    body = "从主页把监听到的包名追加进来，或者先导入一个已有白名单文件。",
                )
            }
        } else {
            items(uiState.whitelistEntries, key = { it }) { packageName ->
                Card(shape = RoundedCornerShape(22.dp)) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            packageName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsRoute(
    uiState: AppUiState,
    actions: AppActions,
    innerPadding: PaddingValues,
) {
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
            Card(shape = RoundedCornerShape(28.dp)) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("监听模式", style = MaterialTheme.typography.titleMedium)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MonitorMode.entries.forEach { mode ->
                            FilterChip(
                                selected = uiState.settings.monitorMode == mode,
                                onClick = { actions.onChangeMonitorMode(mode) },
                                label = {
                                    Text(
                                        when (mode) {
                                            MonitorMode.Standard -> "普通模式"
                                            MonitorMode.EnhancedRoot -> "增强模式"
                                        }
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (mode == MonitorMode.Standard) {
                                            Icons.Outlined.NotificationsActive
                                        } else {
                                            Icons.Outlined.Security
                                        },
                                        contentDescription = null,
                                    )
                                },
                            )
                        }
                    }
                    Text(
                        text = if (uiState.settings.monitorMode == MonitorMode.Standard) {
                            "普通模式使用前台服务动态监听安装广播，适合先快速稳定落地。"
                        } else {
                            "增强模式已预留 root daemon 通道，当前版本会显示 root 状态并继续保留标准监听作为兜底。"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(28.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("开机后自动恢复监听", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "设备启动完成后自动恢复前台服务，避免忘记手动开启。",
                            modifier = Modifier.padding(end = 4.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = uiState.settings.autoStartOnBoot,
                        onCheckedChange = actions.onToggleAutoStart,
                    )
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(28.dp)) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Root 状态", style = MaterialTheme.typography.titleMedium)
                    StatusPill(
                        label = when (uiState.rootStatus.availability) {
                            RootAvailability.Unknown -> "检查中"
                            RootAvailability.Unsupported -> "未接入"
                            RootAvailability.Ready -> "可用"
                        },
                        color = when (uiState.rootStatus.availability) {
                            RootAvailability.Ready -> MaterialTheme.colorScheme.primary
                            RootAvailability.Unsupported -> MaterialTheme.colorScheme.secondary
                            RootAvailability.Unknown -> MaterialTheme.colorScheme.tertiary
                        },
                    )
                    Text(
                        uiState.rootStatus.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = actions.onStartRootDaemon,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("启动 daemon")
                        }
                        OutlinedButton(
                            onClick = actions.onStopRootDaemon,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("停止 daemon")
                        }
                    }
                }
            }
        }
    }
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
                    )
                )
                .padding(22.dp)
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
                    StatusPill(
                        label = when (uiState.settings.monitorMode) {
                            MonitorMode.Standard -> "普通模式"
                            MonitorMode.EnhancedRoot -> "增强模式"
                        },
                        color = Color.White.copy(alpha = 0.18f),
                        contentColor = Color.White,
                    )
                }
                Button(
                    onClick = {
                        actions.onToggleService(uiState.settings.serviceStatus != ServiceStatus.Running)
                    },
                ) {
                    Icon(
                        imageVector = if (uiState.settings.serviceStatus == ServiceStatus.Running) {
                            Icons.Outlined.ToggleOff
                        } else {
                            Icons.Outlined.ToggleOn
                        },
                        contentDescription = null,
                    )
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    Text(
                        if (uiState.settings.serviceStatus == ServiceStatus.Running) {
                            "停止监听服务"
                        } else {
                            "开启监听服务"
                        }
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader(
                title = "白名单文件",
                subtitle = if (uiState.whitelistConfig.usingDefaultFile) "默认文件" else "用户导入文件",
            )
            Text(
                text = if (uiState.whitelistConfig.usingDefaultFile) {
                    "当前未指定文件，应用会在私有目录使用默认格式：第一行 true，后续每行一个包名。"
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
                    onClick = actions.onExportWhitelist,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.FileDownload, contentDescription = null)
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    Text("导出文件")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InstallEventCard(
    event: InstallEvent,
    actions: AppActions,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { targetValue ->
            if (targetValue == SwipeToDismissBoxValue.EndToStart) {
                actions.onDeleteEvent(event.id)
            }
            targetValue != SwipeToDismissBoxValue.StartToEnd
        },
        positionalThreshold = { distance -> distance * 0.35f },
    )
    SwipeToDismissBox(
        state = dismissState,
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = event.appLabel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = event.packageName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        StatusPill(
                            label = when (event.decision) {
                                EventDecision.Pending -> "待处理"
                                EventDecision.Added -> "已添加"
                                EventDecision.Ignored -> "已忽略"
                            },
                            color = when (event.decision) {
                                EventDecision.Pending -> MaterialTheme.colorScheme.tertiaryContainer
                                EventDecision.Added -> MaterialTheme.colorScheme.primaryContainer
                                EventDecision.Ignored -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            contentColor = when (event.decision) {
                                EventDecision.Pending -> MaterialTheme.colorScheme.onTertiaryContainer
                                EventDecision.Added -> MaterialTheme.colorScheme.onPrimaryContainer
                                EventDecision.Ignored -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                    Text(
                        text = "${formatTimestamp(event.installedAt)} · ${
                            if (event.sourceMode == MonitorMode.Standard) "前台服务" else "增强模式"
                        }",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (event.decision == EventDecision.Pending) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { actions.onAddToWhitelist(event.id, event.packageName) },
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Outlined.TaskAlt, contentDescription = null)
                                Spacer(Modifier.padding(horizontal = 4.dp))
                                Text("添加")
                            }
                            OutlinedButton(
                                onClick = { actions.onIgnoreEvent(event.id) },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("忽略")
                            }
                        }
                    }
                }
            }
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
            style = MaterialTheme.typography.bodyLarge,
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

@Composable
private fun MessageCard(text: String, isError: Boolean) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isError) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            }
        ),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            color = if (isError) {
                MaterialTheme.colorScheme.onErrorContainer
            } else {
                MaterialTheme.colorScheme.onSecondaryContainer
            },
        )
    }
}

@Composable
private fun EmptyStateCard(title: String, body: String) {
    Card(shape = RoundedCornerShape(24.dp)) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatusPill(
    label: String,
    color: Color,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(color)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(label, color = contentColor, style = MaterialTheme.typography.labelLarge)
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())
    return formatter.format(Instant.ofEpochMilli(timestamp))
}
