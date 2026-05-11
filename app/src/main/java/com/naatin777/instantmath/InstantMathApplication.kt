package com.naatin777.instantmath

import android.app.Application
import com.naatin777.instantmath.di.appModule
import io.ratex.RaTeXFontLoader
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class InstantMathApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Preload RaTeX fonts
        RaTeXFontLoader.loadFromAssets(this, "fonts")

        startKoin {
            androidLogger()
            androidContext(this@InstantMathApplication)
            modules(appModule)
        }
    }
}
