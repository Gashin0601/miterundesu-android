package com.miterundesu.app

import android.app.Application
import com.miterundesu.app.data.local.AppDatabase
import com.miterundesu.app.manager.LocalizationManager
import com.miterundesu.app.manager.NetworkMonitor
import com.miterundesu.app.manager.OnboardingManager
import com.miterundesu.app.manager.SettingsManager
import com.miterundesu.app.manager.WhatsNewManager

class MiterundesuApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var settingsManager: SettingsManager
        private set

    lateinit var networkMonitor: NetworkMonitor
        private set

    lateinit var onboardingManager: OnboardingManager
        private set

    lateinit var whatsNewManager: WhatsNewManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getInstance(this)
        settingsManager = SettingsManager(this)
        networkMonitor = NetworkMonitor(this)
        onboardingManager = OnboardingManager(this)
        whatsNewManager = WhatsNewManager(this)

        LocalizationManager.initialize(settingsManager)
    }

    override fun onTerminate() {
        super.onTerminate()
        networkMonitor.unregister()
    }

    companion object {
        lateinit var instance: MiterundesuApplication
            private set
    }
}
