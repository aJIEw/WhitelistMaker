package com.ajiew.whitelistmaker.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.ajiew.whitelistmaker.ui.AppRoute
import com.ajiew.whitelistmaker.ui.AppScreen
import com.ajiew.whitelistmaker.ui.theme.WhitelistMakerTheme
import kotlinx.coroutines.flow.emptyFlow

/**
 * Debug 构建下专用的演示入口。
 *
 * 用于在真实设备上预览和截图 UI：直接用 [DemoData] 中的固定假数据渲染 [AppScreen]，
 * 不初始化 `AppContainer`、不读写白名单文件、不启动监听服务。
 *
 * 支持与 [MainActivity] 相同的 `route` extra：传 `"whitelist"` 时直接进入白名单页，
 * 其他值或未传参时进入首页。截图脚本可借此一次拉起指定页面。
 */
class DemoMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WhitelistMakerTheme {
                DemoAppScreen(
                    initialRoute = when (intent.getStringExtra("route")) {
                        "whitelist" -> AppRoute.Whitelist
                        else -> AppRoute.Home
                    },
                )
            }
        }
    }
}

/**
 * 用演示数据装配 [AppScreen]。
 *
 * 这里绕过 `AppViewModel` 和 Repository，直接把 [DemoData.uiState] 与 [DemoData.actions]
 * 交给页面，因此，无需真实业务状态即可渲染完整界面。
 */
@Composable
private fun DemoAppScreen(initialRoute: AppRoute) {
    AppScreen(
        uiState = DemoData.uiState,
        actions = DemoData.actions,
        initialRoute = initialRoute,
        deletedEventGroups = remember { emptyFlow() },
    )
}

@Preview(name = "Demo 首页", showBackground = true)
@Composable
private fun DemoHomePreview() {
    WhitelistMakerTheme {
        DemoAppScreen(initialRoute = AppRoute.Home)
    }
}

@Preview(name = "Demo 白名单", showBackground = true)
@Composable
private fun DemoWhitelistPreview() {
    WhitelistMakerTheme {
        DemoAppScreen(initialRoute = AppRoute.Whitelist)
    }
}
