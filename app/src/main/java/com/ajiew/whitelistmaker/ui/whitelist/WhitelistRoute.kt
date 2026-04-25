package com.ajiew.whitelistmaker.ui.whitelist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ajiew.whitelistmaker.domain.AppUiState
import com.ajiew.whitelistmaker.ui.AppActions
import com.ajiew.whitelistmaker.ui.PreviewDefaults
import com.ajiew.whitelistmaker.ui.components.EmptyStateCard
import com.ajiew.whitelistmaker.ui.components.StatusPill

private data class WhitelistScrollbarState(
    val visibleItems: Int,
    val totalItems: Int,
    val firstVisibleItemIndex: Int,
)

@Composable
internal fun WhitelistRoute(
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
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = if (uiState.whitelistConfig.usingDefaultFile) {
                        "正在使用应用默认白名单文件。"
                    } else {
                        "当前写入文件：${uiState.whitelistConfig.importedDisplayName ?: "已导入文件"}"
                    },
                    style = MaterialTheme.typography.titleMedium,
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
                                            vertical = 10.dp,
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

@Preview
@Composable
private fun WhitelistRoutePreview() {
    WhitelistRoute(
        uiState = PreviewDefaults.uiState,
        actions = PreviewDefaults.actions,
        innerPadding = PaddingValues(),
    )
}

@Composable
private fun WhitelistListScrollbar(
    listState: LazyListState,
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
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
        )
    }
}
