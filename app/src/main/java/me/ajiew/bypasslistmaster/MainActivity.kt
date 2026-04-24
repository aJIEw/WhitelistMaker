package me.ajiew.bypasslistmaster

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import me.ajiew.bypasslistmaster.service.ServiceController
import me.ajiew.bypasslistmaster.ui.AppRoute
import me.ajiew.bypasslistmaster.ui.AppActions
import me.ajiew.bypasslistmaster.ui.AppScreen
import me.ajiew.bypasslistmaster.ui.AppViewModel
import me.ajiew.bypasslistmaster.ui.theme.BypassListMasterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BypassListMasterTheme {
                val viewModel: AppViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val context = LocalContext.current

                val importLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocument(),
                ) { uri ->
                    if (uri != null) {
                        persistUriPermission(uri)
                        viewModel.importWhitelist(uri)
                    }
                }

                val exportLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.CreateDocument("text/plain"),
                ) { uri ->
                    if (uri != null) {
                        viewModel.exportWhitelist(uri)
                    }
                }

                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                ) {
                    ServiceController.start(context)
                }

                val appActions = remember(viewModel, context) {
                    AppActions(
                        onToggleService = { shouldStart ->
                            if (shouldStart) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS,
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    Log.i(TAG, "onCreate: Request notification permission")
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    ServiceController.start(context)
                                }
                            } else {
                                ServiceController.stop(context)
                            }
                        },
                        onImportWhitelist = {
                            importLauncher.launch(arrayOf("text/plain", "*/*"))
                        },
                        onExportWhitelist = {
                            exportLauncher.launch("bypass-whitelist.txt")
                        },
                        onCopyWhitelist = viewModel::copyWhitelistToClipboard,
                        onAddToWhitelist = viewModel::addEventToWhitelist,
                        onIgnoreEvent = viewModel::ignoreEvent,
                        onDeleteEvent = viewModel::deleteEvent,
                        onRestoreDeletedEvent = viewModel::restoreDeletedEvent,
                        onChangeMonitorMode = viewModel::updateMonitorMode,
                        onToggleAutoStart = viewModel::updateAutoStart,
                        onStartRootDaemon = viewModel::startRootDaemon,
                        onStopRootDaemon = viewModel::stopRootDaemon,
                    )
                }

                AppScreen(
                    uiState = uiState,
                    actions = appActions,
                    initialRoute = intent.routeExtra(),
                    deletedEvents = viewModel.deletedEvents,
                )
            }
        }
    }

    private fun persistUriPermission(uri: Uri) {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        runCatching {
            contentResolver.takePersistableUriPermission(uri, flags)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}

private fun Intent?.routeExtra(): AppRoute {
    val route = this?.getStringExtra("route")
    return when (route) {
        "whitelist" -> AppRoute.Whitelist
        "settings" -> AppRoute.Settings
        else -> AppRoute.Home
    }
}
