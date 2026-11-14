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
