package me.ajiew.bypasslistmaster

import android.app.Application
import me.ajiew.bypasslistmaster.core.AppContainer

class BypassListMasterApp : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
