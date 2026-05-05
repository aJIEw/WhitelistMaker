package com.ajiew.whitelistmaker.core

import android.app.Application
import com.ajiew.whitelistmaker.data.AppPreferences
import com.ajiew.whitelistmaker.data.WhitelistRepository
import com.ajiew.whitelistmaker.data.InstallEventStore
import com.ajiew.whitelistmaker.data.WhitelistFileStore

class AppContainer(application: Application) {
    private val preferences = AppPreferences(application)
    private val installEventStore = InstallEventStore(application)
    private val whitelistFileStore = WhitelistFileStore(application, preferences)

    val repository = WhitelistRepository(
        application = application,
        preferences = preferences,
        installEventStore = installEventStore,
        whitelistFileStore = whitelistFileStore,
    )
}
