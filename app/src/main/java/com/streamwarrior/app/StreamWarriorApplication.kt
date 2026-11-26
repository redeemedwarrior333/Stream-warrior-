package com.streamwarrior.app

import android.app.Application
import com.streamwarrior.app.data.SettingsRepository

class StreamWarriorApplication : Application() {
    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(applicationContext)
    }
}
