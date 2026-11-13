package com.ajiew.whitelistmaker.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ajiew.whitelistmaker.domain.AppUiState
import com.ajiew.whitelistmaker.ui.AppActions
import com.ajiew.whitelistmaker.ui.PreviewDefaults
import com.ajiew.whitelistmaker.ui.components.SectionHeader

@Preview
@Composable
private fun WhitelistFileCardPreview() {
    WhitelistFileCard(
        uiState = PreviewDefaults.uiState,
        actions = PreviewDefaults.actions,
    )
}

@Composable
internal fun WhitelistFileCard(
    uiState: AppUiState,
    actions: AppActions,
) {
    var settingsExpanded by rememberSaveable { mutableStateOf(false) }
    Card(shape = RoundedCornerShape(28.dp)) {
        Column(
            modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 12.dp),
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
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                ExpandableSettingsFooter(
                    expanded = settingsExpanded,
                    onExpandedChange = { settingsExpanded = it },
                    contentColor = MaterialTheme.colorScheme.primary,
                    dividerColor = MaterialTheme.colorScheme.outlineVariant,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 4.dp),
                        ) {
                            Text("反转模式", style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "开启时为 true，关闭时为 false。",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = uiState.whitelistConfig.revertEnabled,
                            onCheckedChange = actions.onToggleRevert,
                        )
                    }
                }
            }
        }
    }
}