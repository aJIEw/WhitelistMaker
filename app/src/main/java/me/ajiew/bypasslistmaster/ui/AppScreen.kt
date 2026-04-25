package me.ajiew.bypasslistmaster.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
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
import me.ajiew.bypasslistmaster.domain.WhitelistFileFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private data class WhitelistScrollbarState(
    val visibleItems: Int,
    val totalItems: Int,
    val firstVisibleItemIndex: Int,
)

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
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        bottomBar = {
            NavigationBar {
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
    val sortedWhitelistEntries = remember(uiState.whitelistEntries) {
        uiState.whitelistEntries.sortedWith(String.CASE_INSENSITIVE_ORDER)
    }
    val whitelistListState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 20.dp,
                top = innerPadding.calculateTopPadding() + 12.dp,
                end = 20.dp,
                bottom = innerPadding.calculateBottomPadding() + 12.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (uiState.whitelistConfig.usingDefaultFile) {
                        "正在使用应用默认白名单文件。"
                    } else {
                        "当前写入文件：${uiState.whitelistConfig.importedDisplayName ?: "已导入文件"}"
                    }, style = MaterialTheme.typography.titleMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = actions.onCopyWhitelist,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.size(4.dp))
                        Text("复制到剪贴板", maxLines = 1, softWrap = false)
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("白名单列表", style = MaterialTheme.typography.titleMedium)
                    StatusPill(
                        label = "${uiState.whitelistEntries.size} 个包名",
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        state = whitelistListState,
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (sortedWhitelistEntries.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    title = "白名单还是空的",
                                    body = "从主页把监听到的包名追加进来，或者先导入一个已有白名单文件。",
                                )
                            }
                        } else {
                            items(sortedWhitelistEntries, key = { it }) { packageName ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItem(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                ) {
                                    Text(
                                        text = packageName,
                                        modifier = Modifier.padding(
                                            horizontal = 14.dp,
                                            vertical = 10.dp
                                        ),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        }
                    }
                    WhitelistListScrollbar(
                        listState = whitelistListState,
                        modifier = Modifier.align(Alignment.CenterEnd),
                    )
                }
            }
        }
    }
}

@Composable
private fun WhitelistListScrollbar(
    listState: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier,
) {
    var scrollbarState by remember { mutableStateOf<WhitelistScrollbarState?>(null) }
    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val visibleItems = layoutInfo.visibleItemsInfo.size
            val canScroll = listState.canScrollBackward || listState.canScrollForward
            if (!canScroll || totalItems == 0 || visibleItems == 0) {
                null
            } else {
                WhitelistScrollbarState(
                    visibleItems = visibleItems,
                    totalItems = totalItems,
                    firstVisibleItemIndex = listState.firstVisibleItemIndex,
                )
            }
        }.collect { state ->
            scrollbarState = state
        }
    }
    val state = scrollbarState ?: return
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxHeight()
            .width(8.dp)
            .padding(end = 3.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        val trackHeightPx = constraints.maxHeight.toFloat()
        val minThumbHeightPx = with(density) { 36.dp.toPx() }
        val thumbHeightPx =
            (trackHeightPx * state.visibleItems / state.totalItems).coerceIn(minThumbHeightPx, trackHeightPx)
        val maxOffsetPx = (trackHeightPx - thumbHeightPx).coerceAtLeast(0f)
        val maxFirstVisibleIndex = (state.totalItems - state.visibleItems).coerceAtLeast(1)
        val offsetRatio = state.firstVisibleItemIndex.toFloat() / maxFirstVisibleIndex
        val thumbOffset = with(density) { (maxOffsetPx * offsetRatio).toDp() }
        val thumbHeight = with(density) { thumbHeightPx.toDp() }

        Box(
            modifier = Modifier
                .offset(y = thumbOffset)
                .width(3.dp)
                .height(thumbHeight)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
        )
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("白名单文件格式", style = MaterialTheme.typography.titleMedium)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        WhitelistFileFormat.entries.forEach { format ->
                            FilterChip(
                                selected = uiState.whitelistConfig.format == format,
                                onClick = { actions.onChangeWhitelistFormat(format) },
                                label = { Text(format.displayName) },
                            )
                        }
                    }
                    Text(
                        uiState.whitelistConfig.format.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            onClick = actions.onStartRootDaemon, modifier = Modifier.weight(1f)
                        ) {
                            Text("启动 daemon")
                        }
                        OutlinedButton(
                            onClick = actions.onStopRootDaemon, modifier = Modifier.weight(1f)
                        ) {
                            Text("停止 daemon")
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun HeroStatusCardPreview() {
    HeroStatusCard(
        uiState = AppUiState(),
        actions = AppActions(
            onToggleService = { },
            onImportWhitelist = { },
            onExportWhitelist = { },
            onCopyWhitelist = { },
            onPasteWhitelistFromClipboard = { },
            onAddToWhitelist = { _, _ -> },
            onIgnoreEvent = { },
            onDeleteEvent = { },
            onRestoreDeletedEvent = { },
            onChangeMonitorMode = { },
            onToggleAutoStart = { },
            onChangeWhitelistFormat = { },
            onStartRootDaemon = { },
            onStopRootDaemon = { },
        ),
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
            modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    onClick = actions.onPasteWhitelistFromClipboard, modifier = Modifier.weight(1f)
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
private fun InstallEventCard(
    event: InstallEvent,
    actions: AppActions,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { distance -> distance * 0.35f },
    )
    LaunchedEffect(dismissState.currentValue, event.id) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            actions.onDeleteEvent(event.id)
        }
    }
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
                        text = formatTimestamp(event.installedAt),
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
            modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
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
