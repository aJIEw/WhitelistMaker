package com.ajiew.whitelistmaker.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ajiew.whitelistmaker.BuildConfig
import com.ajiew.whitelistmaker.domain.AppUiState
import com.ajiew.whitelistmaker.domain.ServiceStatus
import com.ajiew.whitelistmaker.ui.AppActions
import com.ajiew.whitelistmaker.ui.PreviewDefaults
import com.ajiew.whitelistmaker.ui.components.StatusPill

@Preview
@Composable
private fun HeroStatusCardPreview() {
    HeroStatusCard(
        uiState = PreviewDefaults.uiState,
        actions = PreviewDefaults.actions,
    )
}

@Composable
internal fun HeroStatusCard(
    uiState: AppUiState,
    actions: AppActions,
) {
    var settingsExpanded by rememberSaveable { mutableStateOf(false) }
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
                .padding(start = 22.dp, top = 22.dp, end = 22.dp, bottom = 12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "安装白名单助手",
                        modifier = Modifier.alignByBaseline(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "v${BuildConfig.VERSION_NAME}",
                        modifier = Modifier.alignByBaseline(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.72f),
                    )
                }
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
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                    ExpandableSettingsFooter(
                        expanded = settingsExpanded,
                        onExpandedChange = { settingsExpanded = it },
                        contentColor = Color.White,
                        dividerColor = Color.White.copy(alpha = 0.24f),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "开机或重启时自动开启监听服务",
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 12.dp),
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                            )
                            Switch(
                                checked = uiState.settings.autoStartOnBoot,
                                onCheckedChange = actions.onToggleAutoStart,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF1BCE71),
                                    uncheckedThumbColor = Color.White.copy(alpha = 0.82f),
                                    uncheckedTrackColor = Color.White.copy(alpha = 0.18f),
                                    uncheckedBorderColor = Color.White.copy(alpha = 0.5f),
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}