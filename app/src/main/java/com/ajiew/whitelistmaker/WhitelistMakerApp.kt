package com.ajiew.whitelistmaker

import android.app.Application
import com.ajiew.whitelistmaker.core.AppContainer

class WhitelistMakerApp : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
