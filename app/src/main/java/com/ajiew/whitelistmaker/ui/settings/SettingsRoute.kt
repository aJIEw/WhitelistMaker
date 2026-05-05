package com.ajiew.whitelistmaker.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ajiew.whitelistmaker.domain.AppUiState
import com.ajiew.whitelistmaker.domain.WhitelistFileFormat
import com.ajiew.whitelistmaker.ui.AppActions
import com.ajiew.whitelistmaker.ui.PreviewDefaults

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SettingsRoute(
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
            AutoStartCard(uiState, actions)
        }
        item {
            WhitelistFormatCard(uiState, actions)
        }
    }
}

@Preview
@Composable
private fun SettingsRoutePreview() {
    SettingsRoute(
        uiState = PreviewDefaults.uiState,
        actions = PreviewDefaults.actions,
        innerPadding = PaddingValues(),
    )
}

@Composable
private fun AutoStartCard(
    uiState: AppUiState,
    actions: AppActions,
) {
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WhitelistFormatCard(
    uiState: AppUiState,
    actions: AppActions,
) {
    Card(shape = RoundedCornerShape(28.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("白名单文件格式", style = MaterialTheme.typography.titleMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
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
