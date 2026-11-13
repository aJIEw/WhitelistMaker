package com.ajiew.whitelistmaker.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ajiew.whitelistmaker.domain.AppUiState
import com.ajiew.whitelistmaker.domain.InstallEventGroups
import com.ajiew.whitelistmaker.ui.AppActions
import com.ajiew.whitelistmaker.ui.PreviewDefaults
import com.ajiew.whitelistmaker.ui.components.EmptyStateCard
import com.ajiew.whitelistmaker.ui.components.MessageCard
import com.ajiew.whitelistmaker.ui.components.SectionHeader

@Preview
@Composable
private fun HomeRoutePreview() {
    HomeRoute(
        uiState = PreviewDefaults.uiState,
        actions = PreviewDefaults.actions,
        innerPadding = PaddingValues(),
    )
}

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

@Composable
fun ExpandableSettingsFooter(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    contentColor: Color,
    dividerColor: Color,
    content: @Composable ColumnScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 32.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Button,
                    onClickLabel = if (expanded) "收起设置" else "展开设置",
                    onClick = { onExpandedChange(!expanded) },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = if (expanded) "收起设置" else "展开设置",
                tint = contentColor,
                modifier = Modifier.size(24.dp),
            )
        }
        if (expanded) {
            HorizontalDivider(
                modifier = Modifier.padding(top = 4.dp),
                color = dividerColor,
            )
            Spacer(Modifier.padding(top = 8.dp))
            content()
        }
    }
}
