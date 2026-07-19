package com.ajiew.whitelistmaker.ui

import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import com.ajiew.whitelistmaker.domain.AppUiState
import com.ajiew.whitelistmaker.domain.InstallEvent
import com.ajiew.whitelistmaker.domain.ServiceStatus
import com.ajiew.whitelistmaker.ui.home.HomeRoute
import com.ajiew.whitelistmaker.ui.settings.SettingsRoute
import com.ajiew.whitelistmaker.ui.whitelist.WhitelistRoute

enum class AppRoute {
    Home, Whitelist, Settings,
}

@Composable
fun AppScreen(
    uiState: AppUiState,
    actions: AppActions,
    initialRoute: AppRoute,
    deletedEventGroups: Flow<List<InstallEvent>>,
) {
    var selectedRoute by rememberSaveable { mutableStateOf(initialRoute) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(deletedEventGroups) {
        deletedEventGroups.collect { deletedEvents ->
            val latestEvent = deletedEvents.maxBy(InstallEvent::installedAt)
            val result = snackbarHostState.showSnackbar(
                message = "${latestEvent.appLabel} 的 ${deletedEvents.size} 条安装记录已删除",
                actionLabel = "撤销",
                withDismissAction = true,
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) {
                actions.onRestoreDeletedEventGroup(deletedEvents)
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        bottomBar = {
            AppBottomBar(
                selectedRoute = selectedRoute,
                serviceStatus = uiState.settings.serviceStatus,
                onRouteSelected = { selectedRoute = it },
            )
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
private fun AppBottomBar(
    selectedRoute: AppRoute,
    serviceStatus: ServiceStatus,
    onRouteSelected: (AppRoute) -> Unit,
) {
    NavigationBar {
        listOf(
            AppRoute.Home to "主页",
            AppRoute.Whitelist to "白名单",
            AppRoute.Settings to "设置",
        ).forEach { (route, label) ->
            NavigationBarItem(
                selected = selectedRoute == route,
                onClick = { onRouteSelected(route) },
                icon = {
                    Icon(
                        imageVector = when (route) {
                            AppRoute.Home -> if (
                                serviceStatus == ServiceStatus.Running ||
                                serviceStatus == ServiceStatus.Starting
                            ) {
                                Icons.Outlined.NotificationsActive
                            } else {
                                Icons.Outlined.NotificationsOff
                            }
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
}
