package com.naatin777.instantmath

import android.app.Application
import android.app.UiModeManager
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.naatin777.instantmath.data.SettingsRepository
import com.naatin777.instantmath.di.appModule
import io.ratex.RaTeXFontLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class InstantMathApplication : Application() {
    private val settingsRepository: SettingsRepository by inject()

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)


    override fun onCreate() {
        super.onCreate()

        RaTeXFontLoader.loadFromAssets(this, "fonts")

        startKoin {
            androidLogger()
            androidContext(this@InstantMathApplication)
            modules(appModule)
        }

        applicationScope.launch {
            settingsRepository.themeModeFlow.collect { mode ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val uiModeManager = getSystemService(UiModeManager::class.java)
                    val uiMode = when (mode) {
                        AppCompatDelegate.MODE_NIGHT_NO -> UiModeManager.MODE_NIGHT_NO
                        AppCompatDelegate.MODE_NIGHT_YES -> UiModeManager.MODE_NIGHT_YES
                        else -> UiModeManager.MODE_NIGHT_AUTO
                    }
                    uiModeManager?.setApplicationNightMode(uiMode)
                } else {
                    AppCompatDelegate.setDefaultNightMode(mode)
                }
            }
        }
    }
}
